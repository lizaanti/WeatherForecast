package com.example.weatherforecast.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.data.entities.Forecast;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.data.entities.WeatherData;
import com.example.weatherforecast.model.Weather;
import com.example.weatherforecast.repository.WeatherRepository;
import com.example.weatherforecast.ui.adapter.HourlyForecastAdapter;
import com.example.weatherforecast.ui.adapter.WeatherAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";
    private static final String TAG = "MainActivity";
    private RecyclerView hourlyForecastRecycler;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private WeatherRepository weatherRepository;
    private ExecutorService executorService;

    private TextView cityName;
    private TextView tempResult;
    private TextView forecastResult;
    private TextView feels;
    private SearchView searchView;
    private LocationManager locationManager;
    private ImageView viewIcon;
    private ImageView settings;
    private TextView tvHumidity, tvWind, tvPressure;
    private RecyclerView recyclerView;
    private ArrayList<Weather> forecastList;
    private WeatherAdapter weatherAdapter;
    private List<Forecast> savedForecasts = new ArrayList<>();
    private RequestQueue requestQueue;
    private FusedLocationProviderClient fusedLocationClient;

    private MapView mapView;
    private PlacemarkMapObject placemark;
    private MapObjectCollection mapObjects;
    private com.yandex.mapkit.layers.Layer precipitationLayer;

    @SuppressLint({"WrongThread", "MissingSuperCall"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapview);

        com.yandex.mapkit.map.Map map = mapView.getMap();
        map.move(
                new CameraPosition(
                        new Point(51.5074, -0.1278),
                        10.0f,
                        150.0f,
                        30.0f
                )
        );

        mapObjects = map.getMapObjects();

        ImageProvider imageProvider = ImageProvider.fromResource(this, R.drawable.mark);

        Point initialPoint = new Point(55.7558, 37.6176);
        placemark = mapObjects.addPlacemark(initialPoint);
        placemark.setIcon(imageProvider);
        placemark.setOpacity(0.8f);
        placemark.setDraggable(true);
        placemark.addTapListener((mapObject, point) -> {
            Toast.makeText(
                    MainActivity.this,
                    "Метка: " + point.getLatitude() + ", " + point.getLongitude(),
                    Toast.LENGTH_SHORT
            ).show();
            return true;
        });

        map.move(
                new CameraPosition(initialPoint, 10.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 1f),
                null
        );

        map.setRotateGesturesEnabled(false);
        map.setTiltGesturesEnabled(false);
        map.setZoomGesturesEnabled(true);
        map.setScrollGesturesEnabled(true);

        map.addInputListener(new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) { }
            @Override
            public void onMapLongTap(Map map, Point point) {
                // Плавно перемещаем маркер
                placemark.setGeometry(point);
                // И плавно двигаем камеру к нему
                map.move(
                        new CameraPosition(point, map.getCameraPosition().getZoom(), 0, 0),
                        new Animation(Animation.Type.SMOOTH, 0.5f),
                        null
                );
                updateWeatherForLocation(point);
                Toast.makeText(
                        MainActivity.this,
                        "Метка на: " + point.getLatitude() + ", " + point.getLongitude(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Button shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> shareLocation());

        hourlyForecastRecycler = findViewById(R.id.hourly_forecast_recycler);
        hourlyForecastRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hourlyForecastAdapter = new HourlyForecastAdapter(this);
        hourlyForecastRecycler.setAdapter(hourlyForecastAdapter);
        weatherRepository = new WeatherRepository(getApplicationContext());
        fetchHourlyForecast();

        // Инициализация UI
        cityName = findViewById(R.id.CityNameTV);
        feels = findViewById(R.id.ConditionTV);
        tempResult = findViewById(R.id.Temperature);
        forecastResult = findViewById(R.id.forecastText);
        searchView = findViewById(R.id.SearchView);
        recyclerView = findViewById(R.id.recycleV);
        viewIcon = findViewById(R.id.ViewIcon);
        settings = findViewById(R.id.Settings);
        tvHumidity = findViewById(R.id.TV1);
        tvWind = findViewById(R.id.TV3);
        tvPressure = findViewById(R.id.TV5);
        forecastList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, forecastList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        recyclerView.setAdapter(weatherAdapter);
        weatherRepository = new WeatherRepository(this);
        requestQueue = Volley.newRequestQueue(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Проверка на подключение к интернету
        if (!isNetworkAvailable()) {
            loadOfflineData();
        } else {
            getLocationAndWeather();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM_TOKEN", "Token:" + task.getResult());
                    }
                });

        FirebaseMessaging.getInstance()
                .subscribeToTopic("weather_updates")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to weather_updates");
                    }
                });

        setDynamicBackground();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_CODE);
        } else {
            getLocationAndWeather();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    getWeatherInfo(query.trim());
                } else {
                    Toast.makeText(MainActivity.this, "Введите название города", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        forecastResult.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForecastDetailActivity.class);
            intent.putExtra("forecastList", forecastList);
            startActivity(intent);
        });

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(requestQueue != null){
            requestQueue.cancelAll("weatherRequest");
        }
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void getLocationAndWeather() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if(location != null){
                            String city = getCityName(location.getLongitude(), location.getLatitude());

                            Location entitiesLocation = new Location();
                            entitiesLocation.setLatitude(location.getLatitude());
                            entitiesLocation.setLongitude(location.getLongitude());
                            entitiesLocation.setCityName(city);

                            weatherRepository.insertLocation(entitiesLocation);

                            getWeatherInfo(city);
                        }else{
                            Toast.makeText(this, "Неудалось получить местположение. Введите город вручную.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка определения местоположения.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_CODE);
        }
    }
    private String getCityName(double longitude, double latitude) {
        String cityNameStr = "Не найдено";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null && adr.getLocality() != null && !adr.getLocality().isEmpty()) {
                    cityNameStr = adr.getLocality();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityNameStr;
    }

    private void getWeatherInfo(String city) {

        String currentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                "&appid=" + API_KEY + "&units=metric&lang=ru";
        String forecastWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city +
                "&appid=" + API_KEY + "&units=metric&lang=ru";

        JsonObjectRequest currentWeatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                currentWeatherUrl,
                null,
                response -> {
                    parseCurrentWeatherResponse(response, city);
                },
                error -> {
                    Toast.makeText(MainActivity.this,
                            "Ошибка получения данных текущей погоды.", Toast.LENGTH_SHORT).show();
                }
        );
        currentWeatherRequest.setTag("weatherRequest"); // Установка тега для метода onStop()
        JsonObjectRequest forecastWeatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                forecastWeatherUrl,
                null,
                response -> {
                    parseForecastWeatherResponse(response);
                },
                error -> {
                    Toast.makeText(MainActivity.this,
                            "Ошибка получения данных прогноза.", Toast.LENGTH_SHORT).show();
                }
        );
        forecastWeatherRequest.setTag("weatherRequest");

        if (requestQueue != null) {
        requestQueue.add(currentWeatherRequest);
        requestQueue.add(forecastWeatherRequest);
        } else {
            Toast.makeText(this, "Ошибка: RequestQueue не инициализирована", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void parseCurrentWeatherResponse(JSONObject response, String city) {
        try {
            JSONObject mainObj = response.getJSONObject("main");
            int temp = (int) Math.round(mainObj.getDouble("temp"));
            int feelsLike = (int) Math.round(mainObj.getDouble("feels_like"));
            cityName.setText(city);
            tempResult.setText(temp + "°C");
            feels.setText("Ощущается как: " + feelsLike + "°C");

            int humidity = mainObj.getInt("humidity");
            int pressure = mainObj.getInt("pressure");
            JSONObject windObj = response.getJSONObject("wind");
            double windSpeed = windObj.getDouble("speed");

            tvHumidity.setText(humidity + "%");
            tvWind.setText(Math.round(windSpeed) + " м/с");
            tvPressure.setText(pressure + " hPa");

            WeatherData weatherData = new WeatherData();

            weatherData.setCityName(city);
            weatherData.setHumidity(humidity);
            weatherData.setPressure(pressure);
            weatherData.setTemperature(temp);
            weatherData.setWindSpeed(windSpeed);

            JSONArray weatherArray = response.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                String icon = weatherObj.getString("icon");
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                Picasso.get().load(iconUrl)
                        .placeholder(R.drawable.weather)
                        .into(viewIcon);
                weatherData.setWeatherIcon(iconUrl);
            }

            weatherRepository.insertWeatherData(weatherData, city);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Ошибка обработки данных текущей погоды.", Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void parseForecastWeatherResponse(JSONObject response) {
        try {
            JSONArray list = response.getJSONArray("list");
            forecastList.clear();
            savedForecasts.clear();

            for (int i = 0; i < list.length(); i += 8) {
                JSONObject forecastItem = list.getJSONObject(i);
                String dateTime = forecastItem.getString("dt_txt").split(" ")[0];
                JSONObject mainObj = forecastItem.getJSONObject("main");
                int temp = (int) Math.round(mainObj.getDouble("temp"));
                int feelsLike = (int) Math.round(mainObj.getDouble("feels_like"));
                String icon = forecastItem.getJSONArray("weather")
                        .getJSONObject(0).getString("icon");
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                forecastList.add(new Weather(dateTime, String.valueOf(temp), iconUrl, String.valueOf(feelsLike)));

                Forecast forecast = new Forecast();
                forecast.setDateTime(dateTime);
                forecast.setTemperature((double) temp);
                forecast.setFeelsLike((double) feelsLike);
                forecast.setWeatherIcon(icon);
                savedForecasts.add(forecast);
            }
            weatherAdapter.notifyDataSetChanged();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> weatherRepository.insertForecasts(savedForecasts));
            executor.shutdown();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Ошибка обработки данных прогноза погоды.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void loadOfflineData(){
        weatherRepository.getAllForecasts().observe(this, forecasts -> {
            if(forecasts != null){
                forecastList.clear();
                for(Forecast f : forecasts){
                    if(f == null) continue;

                    String date = f.getDateTime() != null ? f.getDateTime() : "";
                    String temp = String.valueOf(f.getTemperature()) != null ? String.valueOf(f.getTemperature()) : "";
                    String feels = String.valueOf(f.getFeelsLike()) != null ? String.valueOf(f.getFeelsLike()) : "";

                    String iconCode = f.getWeatherIcon();
                    String iconUrl = (iconCode != null && !iconCode.isEmpty())
                            ? "https://openweathermap.org/img/wn/" + iconCode + "@2x.png"
                            : null;
                    forecastList.add(new Weather(date, temp, iconUrl, feels));
                }
                if(weatherAdapter != null){
                    weatherAdapter.notifyDataSetChanged();
                }
            }
        });
        weatherRepository.getMostRecentWeather().observe(this, weatherData ->  {
                if(weatherData != null){
                    String city = weatherData.getCityName() != null ? weatherData.getCityName() : "N/A";
                    cityName.setText(city);

                    String temperature = String.valueOf(weatherData.getTemperature()) != null ? String.valueOf(weatherData.getTemperature()) : "0";
                    tempResult.setText(temperature + " °C");

                    String humidity = String.valueOf(weatherData.getHumidity()) != null ? String.valueOf(weatherData.getHumidity()) : "0";
                    tvHumidity.setText(humidity + " %");

                    String wind = String.valueOf(weatherData.getWindSpeed()) != null ? String.valueOf(weatherData.getWindSpeed()) : "0";
                    tvWind.setText(wind + " м/с");

                    String pressure = String.valueOf(weatherData.getPressure()) != null ? String.valueOf(weatherData.getPressure()) : "0";
                    tvPressure.setText(pressure + " hPa");

                    String iconUrl = weatherData.getWeatherIcon();
                    if(iconUrl != null && !iconUrl.isEmpty()){
                        Picasso.get()
                                .load(iconUrl)
                                .placeholder(R.drawable.weather)
                                .into(viewIcon);
                    }else{
                        viewIcon.setImageResource(R.drawable.weather);
                    }

                }
        });

    }

    private void setDynamicBackground() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int backgroundResId;
        if (hour >= 6 && hour < 12) {
            backgroundResId = R.drawable.day_bg;
        } else if (hour >= 12 && hour < 18) {
            backgroundResId = R.drawable.eve_bg;
        } else {
            backgroundResId = R.drawable.night_bg;
        }
        LinearLayout mainLayout = findViewById(R.id.main);
        mainLayout.setBackgroundResource(backgroundResId);
    }

    private void shareLocation() {
        Point point = placemark.getGeometry();
        double longitude = point.getLongitude();
        double latitude = point.getLatitude();
        CameraPosition cameraPosition = mapView.getMap().getCameraPosition();
        double zoom = cameraPosition.getZoom();

        String url = "https://yandex.ru/maps/?ll=" + longitude + "," + latitude + "&z=15";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Посмотрите это место на карте: " + url);
        startActivity(Intent.createChooser(shareIntent, "Поделиться местоположением"));
    }

    private void fetchHourlyForecast() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            Location location = weatherRepository.getLatestLocation();
            String city = (location != null && location.getCityName() != null && !location.getCityName().isEmpty())
                    ? location.getCityName() : "Moscow";
            Log.d(TAG, "Selected city: " + city);

            runOnUiThread(() -> {
                String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";
                Log.d(TAG, "API URL: " + url);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        response -> {
                            Log.d(TAG, "API response: " + response.toString());
                            List<HourlyForecast> forecasts = parseHourlyForecast(response);
                            hourlyForecastAdapter.setForecasts(forecasts);
                            addPrecipitationMarkers(response);
                        },
                        error -> {
                            Log.e(TAG, "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), error);
                        });

                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(request);
            });
        });

        executor.shutdown();
    }

    private List<HourlyForecast> parseHourlyForecast(JSONObject response) {
        List<HourlyForecast> forecasts = new ArrayList<>();

        try {
            if (!response.has("list")) {
                Log.e(TAG, "No 'list' array in response");
                return forecasts;
            }

            JSONArray forecastArray = response.getJSONArray("list");
            // Берем первые 8 записей (24 часа)
            for (int i = 0; i < Math.min(8, forecastArray.length()); i++) {
                JSONObject forecast = forecastArray.getJSONObject(i);

                long timestamp = forecast.getLong("dt") * 1000; // Конвертируем в миллисекунды
                JSONObject mainObj = forecast.getJSONObject("main");
                double temp = mainObj.getDouble("temp");

                String icon = "";
                JSONArray weatherArray = forecast.getJSONArray("weather");
                if (weatherArray.length() > 0) {
                    icon = weatherArray.getJSONObject(0).getString("icon");
                }

                forecasts.add(new HourlyForecast(timestamp, temp, icon));
                Log.d(TAG, "Parsed hour: time=" + timestamp + ", temp=" + temp + ", icon=" + icon);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }

        return forecasts;
    }

    private void addPrecipitationMarkers(JSONObject forecastResponse) {
        try {
            Log.d(TAG, "Forecast response: " + forecastResponse.toString());
            JSONArray list = forecastResponse.getJSONArray("list");
            boolean hasPrecipitation = false;
            for (int i = 0; i < Math.min(8, list.length()); i++) {
                JSONObject forecast = list.getJSONObject(i);
                Log.d(TAG, "Forecast item " + i + ": " + forecast.toString());
                if (forecast.has("rain") && forecast.getJSONObject("rain").has("3h") && forecast.getJSONObject("rain").getDouble("3h") > 0) {
                    hasPrecipitation = true;
                    Log.d(TAG, "Rain found: " + forecast.getJSONObject("rain").getDouble("3h") + " mm");
                    break;
                }
                if (forecast.has("snow") && forecast.getJSONObject("snow").has("3h") && forecast.getJSONObject("snow").getDouble("3h") > 0) {
                    hasPrecipitation = true;
                    Log.d(TAG, "Snow found: " + forecast.getJSONObject("snow").getDouble("3h") + " mm");
                    break;
                }
            }

            if (hasPrecipitation) {
                Log.d(TAG, "Attempting to add precipitation marker");
                executorService.execute(() -> {
                    Location location = weatherRepository.getLatestLocation();
                    final Point point;
                    if (location != null) {
                        point = new Point(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Location for marker: (" + point.getLatitude() + ", " + point.getLongitude() + ")");
                    } else {
                        point = new Point(51.5074, -0.1278); // Лондон по умолчанию
                        Log.w(TAG, "Using default London location");
                    }
                    runOnUiThread(() -> {
                        try {
                            mapObjects.clear(); // Очистка старых маркеров
                            PlacemarkMapObject precipitationMark = mapObjects.addPlacemark(
                                    new Point(point.getLatitude() + 0.01, point.getLongitude() + 0.01)
                            );
                            precipitationMark.setIcon(ImageProvider.fromResource(MainActivity.this, R.drawable.water));
                            precipitationMark.setOpacity(1.0f);
                            mapView.getMap().getMapObjects().setVisible(true);
                            mapView.getMap().move(mapView.getMap().getCameraPosition()); // Обновление карты
                            Log.d(TAG, "Precipitation marker added at: (" + (point.getLatitude() + 0.01) + ", " + (point.getLongitude() + 0.01) + ")");
                        } catch (Exception e) {
                            Log.e(TAG, "Error adding precipitation marker: " + e.getMessage(), e);
                        }
                    });
                });
            } else {
                Log.d(TAG, "No precipitation found in forecast");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing precipitation data: " + e.getMessage());
        }
    }

    private void updateWeatherForLocation(Point point){
        String city = getCityName(point.getLongitude(), point.getLatitude());
        if(!city.equals("Не найдено")){
            getWeatherInfo(city);
        }
        else {
            Toast.makeText(this, "Не удалось определить город для этой точки", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndWeather();
            } else {
                Toast.makeText(this,
                        "Разрешение на доступ к местоположению необходимо.", Toast.LENGTH_LONG).show();
            }
        }
    }

}