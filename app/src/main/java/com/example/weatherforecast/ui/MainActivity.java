package com.example.weatherforecast.ui;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.data.AppDatabase;
import com.example.weatherforecast.data.dao.WeatherDataDao;
import com.example.weatherforecast.data.entities.Forecast;
import com.example.weatherforecast.data.entities.UserPreferences;
import com.example.weatherforecast.data.entities.WeatherData;
import com.example.weatherforecast.model.Weather;
import com.example.weatherforecast.repository.WeatherRepository;
import com.example.weatherforecast.ui.adapter.WeatherAdapter;
import com.squareup.picasso.Picasso;

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

import com.example.weatherforecast.data.notifications.MyFirebaseService;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";
    private static final String TAG = "MainActivity";

    private TextView cityName, tempResult, forecastResult, feels;
    private ProgressBar progressBar;
    private SearchView searchView;
    private LocationManager locationManager;
    private ImageView viewIcon;
    private ImageView settings;
    private TextView tvHumidity, tvWind, tvPressure;
    private RecyclerView recyclerView;
    private ArrayList<Weather> forecastList;
    private WeatherAdapter weatherAdapter;

    private WeatherRepository weatherRepository;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        // Перенос clearAllTables в фоновый поток
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            WeatherDataDao weatherDataDao = AppDatabase.getInstance(getApplicationContext()).weatherDataDao();
            weatherDataDao.deleteOldData(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // Удаление данных старше 24 часов
        });
        executor.shutdown();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String token = task.getResult();
                        //Log.d(TAG, "FCM Token: " + token);
                        Log.d("FCM_TOKEN", "Token:" + token);
                   }
                   else{
                       Log.w(TAG, "Fetching FCM token failed", task.getException());
                    }
                });

        // Примерная подписка на тему weather_updates
        FirebaseMessaging.getInstance()
                .subscribeToTopic("weather_updates")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "Subscribed to weather_updates");
                    }
                });

        // Инициализация UI
        cityName = findViewById(R.id.CityNameTV);
        feels = findViewById(R.id.ConditionTV);
        tempResult = findViewById(R.id.Temperature);
        forecastResult = findViewById(R.id.forecastText);
        progressBar = findViewById(R.id.Loading);
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
        recyclerView.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Инициализация репозитория
        weatherRepository = new WeatherRepository(this);

        weatherRepository = new WeatherRepository(this);
        AppDatabase.getInstance(this).clearAllTables(); // Очистка таблиц
        weatherRepository.insertTestData();

        // Добавляем тестовые данные (если нужно)
        weatherRepository.insertTestData();

        // Наблюдаем за данными и выводим их в консоль
        observeDatabase();

        // Остальной код (проверка разрешений, обработчики и т.д.) остается без изменений
        setDynamicBackground();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
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

    private void observeDatabase() {
        // Вывод местоположений
        weatherRepository.getAllLocations().observe(this, locations -> {
            Log.d(TAG, "=== Locations Table ===");
            if (locations == null || locations.isEmpty()) {
                Log.d(TAG, "No locations found.");
            } else {
                for (com.example.weatherforecast.data.entities.Location location : locations) {
                    Log.d(TAG, "ID: " + location.getId() +
                            ", City: " + location.getCityName() +
                            ", Latitude: " + location.getLatitude() +
                            ", Longitude: " + location.getLongitude() +
                            ", Is Favorite: " + location.isFavorite());
                }
            }
        });

        // Вывод данных о погоде
        weatherRepository.getAllWeatherData().observe(this, weatherDataList -> {
            Log.d(TAG, "=== WeatherData Table ===");
            if (weatherDataList == null || weatherDataList.isEmpty()) {
                Log.d(TAG, "No weather data found.");
            } else {
                for (WeatherData weatherData : weatherDataList) {
                    Log.d(TAG, "ID: " + weatherData.getId() +
                            ", Location ID: " + weatherData.getLocationId() +
                            ", Temperature: " + weatherData.getTemperature() +
                            ", Humidity: " + weatherData.getHumidity() +
                            ", Pressure: " + weatherData.getPressure() +
                            ", Weather Icon: " + weatherData.getWeatherIcon() +
                            ", Timestamp: " + weatherData.getTimestamp());
                }
            }
        });

        // Вывод прогнозов
        weatherRepository.getAllForecasts().observe(this, forecasts -> {
            Log.d(TAG, "=== Forecasts Table ===");
            if (forecasts == null || forecasts.isEmpty()) {
                Log.d(TAG, "No forecasts found.");
            } else {
                for (Forecast forecast : forecasts) {
                    Log.d(TAG, "ID: " + forecast.getId() +
                            ", Weather Data ID: " + forecast.getWeatherDataId() +
                            ", DateTime: " + forecast.getDateTime() +
                            ", Temperature: " + forecast.getTemperature() +
                            ", Feels Like: " + forecast.getFeelsLike() +
                            ", Weather Icon: " + forecast.getWeatherIcon());
                }
            }
        });

        // Вывод настроек
        weatherRepository.getAllPreferences().observe(this, preferencesList -> {
            Log.d(TAG, "=== UserPreferences Table ===");
            if (preferencesList == null || preferencesList.isEmpty()) {
                Log.d(TAG, "No preferences found.");
            } else {
                for (UserPreferences preferences : preferencesList) {
                    Log.d(TAG, "ID: " + preferences.getId() +
                            ", Temperature Unit: " + preferences.getTemperatureUnit() +
                            ", Wind Speed Unit: " + preferences.getWindSpeedUnit() +
                            ", Notifications Enabled: " + preferences.isNotificationsEnabled());
                }
            }
        });
    }

    // Остальной код (setDynamicBackground, getLocationAndWeather и т.д.) остается без изменений
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

    private void getLocationAndWeather() {
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                String city = getCityName(location.getLongitude(), location.getLatitude());
                getWeatherInfo(city);
            } else {
                Toast.makeText(this, "Не удалось определить местоположение", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка доступа к местоположению", Toast.LENGTH_SHORT).show();
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
        progressBar.setVisibility(android.view.View.VISIBLE);

        String currentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                "&appid=" + API_KEY + "&units=metric&lang=ru";
        String forecastWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city +
                "&appid=" + API_KEY + "&units=metric&lang=ru";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest currentWeatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                currentWeatherUrl,
                null,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    parseCurrentWeatherResponse(response, city);
                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Ошибка получения данных текущей погоды.", Toast.LENGTH_SHORT).show();
                }
        );

        JsonObjectRequest forecastWeatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                forecastWeatherUrl,
                null,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    parseForecastWeatherResponse(response);
                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Ошибка получения данных прогноза.", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(currentWeatherRequest);
        requestQueue.add(forecastWeatherRequest);
    }

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

            JSONArray weatherArray = response.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                String icon = weatherObj.getString("icon");
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                Picasso.get().load(iconUrl)
                        .placeholder(R.drawable.weather)
                        .into(viewIcon);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Ошибка обработки данных текущей погоды.", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseForecastWeatherResponse(JSONObject response) {
        try {
            JSONArray list = response.getJSONArray("list");
            forecastList.clear();
            for (int i = 0; i < list.length(); i += 8) {
                JSONObject forecastItem = list.getJSONObject(i);
                String dateTime = forecastItem.getString("dt_txt").split(" ")[0];
                JSONObject mainObj = forecastItem.getJSONObject("main");
                int temp = (int) Math.round(mainObj.getDouble("temp"));
                String feelsLike = String.valueOf((int) Math.round(mainObj.getDouble("feels_like")));
                String icon = forecastItem.getJSONArray("weather")
                        .getJSONObject(0).getString("icon");
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                forecastList.add(new Weather(dateTime, String.valueOf(temp), iconUrl, feelsLike));
            }
            weatherAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Ошибка обработки данных прогноза погоды.", Toast.LENGTH_SHORT).show();
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
                        "Разрешение на доступ к местоположению необходимо.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}