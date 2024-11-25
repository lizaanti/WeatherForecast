package com.example.weatherforecast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";

    private TextView cityName, tempResult, forecastResult;
    private ProgressBar progressBar;
    private SearchView searchView;
    private LocationManager locationManager;
    private TextView feels;
    private ImageView icon;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.CityNameTV);
        feels = findViewById(R.id.ConditionTV);
        tempResult = findViewById(R.id.Temperature);
        forecastResult = findViewById(R.id.forecastText);
        progressBar = findViewById(R.id.Loading);
        searchView = findViewById(R.id.SearchView);
        icon = findViewById(R.id.ViewIcon);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else {
            getLocationAndWeather();
        }

        // Поиск погоды по введенному городу
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    getWeatherInfo(query);
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
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndWeather() {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            String city = getCityName(location.getLongitude(), location.getLatitude());
            getWeatherInfo(city);
        } else {
            Toast.makeText(this, "Не удалось определить местоположение", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityNameStr = "Не найдено";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.isEmpty()) {
                        cityNameStr = city;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityNameStr;
    }

    private void getWeatherInfo(String city) {
        progressBar.setVisibility(View.VISIBLE);

        String currentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";
        String forecastWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Запрос текущей погоды
        JsonObjectRequest currentWeatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                currentWeatherUrl,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    parseCurrentWeatherResponse(response, city);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Ошибка получения данных текущей погоды.", Toast.LENGTH_SHORT).show();
                }
        );

        // Запрос прогноза на 5 дней
        JsonObjectRequest forecastWeatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                forecastWeatherUrl,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    parseForecastWeatherResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Ошибка получения данных прогноза.", Toast.LENGTH_SHORT).show();
                }
        );

        // Выполняем запросы
        requestQueue.add(currentWeatherRequest);
        requestQueue.add(forecastWeatherRequest);
    }

    private void parseCurrentWeatherResponse(JSONObject response, String city) {
        try {
            JSONObject main = response.getJSONObject("main");
            String temp = main.getString("temp");
            String feelsLike = main.getString("feels_like");
            long tempRounded = Math.round(Double.parseDouble(temp));
            long feelsRounded = Math.round(Double.parseDouble(feelsLike));
            cityName.setText(city);
            tempResult.setText(tempRounded + "°C");
            feels.setText("Ощущается как: " + feelsLike + "°C");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка обработки текущей погоды.", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseForecastWeatherResponse(JSONObject response) {
        try {
            JSONArray list = response.getJSONArray("list");
            StringBuilder forecast = new StringBuilder("Прогноз на 5 дней:\n");

            for (int i = 0; i < list.length(); i += 8) { // Каждые 8 записей - прогноз на новый день
                JSONObject forecastItem = list.getJSONObject(i);
                String dateTime = forecastItem.getString("dt_txt").split(" ")[0]; // Только дата
                JSONObject main = forecastItem.getJSONObject("main");
                double temp = main.getDouble("temp");
                String description = forecastItem.getJSONArray("weather").getJSONObject(0).getString("description");

                // Форматируем прогноз
                forecast.append(dateTime).append(": ")
                        .append(String.format("%.1f", temp)).append("°C, ")
                        .append(description).append("\n");
            }

            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(400, 400);
            layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
            icon.setLayoutParams(layoutParams2);
            icon.setImageResource(R.drawable.sunny);

            // Устанавливаем текст в TextView
            forecastResult.setText(forecast.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка обработки данных прогноза погоды.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndWeather();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению необходимо.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
