package com.example.weatherforecast.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.adapter.WeatherAdapter;
import com.example.weatherforecast.model.Weather;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";

    private TextView cityName, tempResult, forecastResult, feels;
    private ProgressBar progressBar;
    private SearchView searchView;
    private LocationManager locationManager;
    private ImageView viewIcon; // Главное изображение
    private ImageView settings;

    // TextView для данных о влажности, ветре и давлении
    private TextView tvHumidity, tvHumidityLabel;
    private TextView tvWind, tvWindLabel;
    private TextView tvPressure, tvPressureLabel;

    // RecyclerView для прогноза
    private RecyclerView recyclerView;
    private ArrayList<Weather> forecastList;
    private WeatherAdapter weatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Полноэкранный режим
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        // Привязка view
        cityName = findViewById(R.id.CityNameTV);
        feels = findViewById(R.id.ConditionTV);
        tempResult = findViewById(R.id.Temperature);
        forecastResult = findViewById(R.id.forecastText);
        progressBar = findViewById(R.id.Loading);
        searchView = findViewById(R.id.SearchView);
        recyclerView = findViewById(R.id.recycleV);
        viewIcon = findViewById(R.id.ViewIcon); // Главная иконка
        settings = findViewById(R.id.Settings);

        // Привязка новых TextView для данных о погоде (нижний блок)
        tvHumidity = findViewById(R.id.TV1);
        tvWind = findViewById(R.id.TV3);
        tvPressure = findViewById(R.id.TV5);

        // Инициализация RecyclerView
        forecastList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, forecastList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Смена фона в зависимости от времени суток
        setDynamicBackground();

        // Проверка разрешений
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

        // Поиск погоды по введённому городу
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

        // Обработчик клика для открытия подробного прогноза
        forecastResult.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForecastDetailActivity.class);
            intent.putExtra("forecastList", forecastList);
            startActivity(intent);
        });

        //Обработчик клика для открытия настроек
        settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
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

        // Запрос текущей погоды
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

        // Запрос прогноза на 5 дней
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

            // Получаем данные для нижнего блока
            int humidity = mainObj.getInt("humidity");
            int pressure = mainObj.getInt("pressure");
            JSONObject windObj = response.getJSONObject("wind");
            double windSpeed = windObj.getDouble("speed");

            tvHumidity.setText(humidity + "%");

            tvWind.setText(Math.round(windSpeed) + " м/с");

            tvPressure.setText(pressure + " hPa");

            // Обновляем иконку главного экрана (ViewIcon)
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
