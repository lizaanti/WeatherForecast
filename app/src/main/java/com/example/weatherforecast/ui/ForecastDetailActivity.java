package com.example.weatherforecast.ui;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.model.DailyForecast;
import com.example.weatherforecast.repository.WeatherRepository;
import com.example.weatherforecast.ui.adapter.WeatherAdapter;
import com.example.weatherforecast.model.Weather;
import com.example.weatherforecast.ui.adapter.WeeklyForecastAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForecastDetailActivity extends AppCompatActivity {

    private RecyclerView detailRecyclerView;
    private ArrayList<Weather> detailedForecastList;
    private WeatherAdapter weatherAdapter;
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";
    private static final String TAG = "ForecastDetailActivity";
    private RecyclerView weeklyForecastRecycler;
    private WeeklyForecastAdapter weeklyForecastAdapter;
    private WeatherRepository weatherRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_forecast);

        weeklyForecastRecycler = findViewById(R.id.weekly_forecast_recycler);
        weeklyForecastRecycler.setLayoutManager(new LinearLayoutManager(this));
        weeklyForecastAdapter = new WeeklyForecastAdapter(this);
        weeklyForecastRecycler.setAdapter(weeklyForecastAdapter);

        // Инициализация репозитория
        weatherRepository = new WeatherRepository(getApplicationContext());

        // Получение недельного прогноза
        fetchWeeklyForecast();
    }

    private void fetchWeeklyForecast() {
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
                            List<DailyForecast> dailyForecasts = parseWeeklyForecast(response);
                            weeklyForecastAdapter.setDailyForecasts(dailyForecasts);
                        },
                        error -> {
                            Log.e(TAG, "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), error);
                            // Показать ошибку в UI, если нужно
                        });

                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(request);
            });
        });

        executor.shutdown();
    }

    private List<DailyForecast> parseWeeklyForecast(JSONObject response) {
        List<DailyForecast> dailyForecasts = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            if (!response.has("list")) {
                Log.e(TAG, "No 'list' array in response");
                return dailyForecasts;
            }

            JSONArray forecastArray = response.getJSONArray("list");
            // Группируем по дням
            List<List<HourlyForecast>> dailyHours = new ArrayList<>();
            List<Double> dailyTemps = new ArrayList<>();
            List<Long> dailyDates = new ArrayList<>();

            String lastDate = "";
            List<HourlyForecast> currentDayHours = new ArrayList<>();
            double tempSum = 0;
            int tempCount = 0;

            for (int i = 0; i < forecastArray.length(); i++) {
                JSONObject forecast = forecastArray.getJSONObject(i);
                long timestamp = forecast.getLong("dt") * 1000;
                String date = dateFormat.format(new Date(timestamp));

                if (!date.equals(lastDate) && !lastDate.isEmpty()) {
                    // Новый день
                    dailyHours.add(new ArrayList<>(currentDayHours));
                    dailyDates.add(currentDayHours.get(0).getTimestamp());
                    dailyTemps.add(tempSum / tempCount);
                    currentDayHours.clear();
                    tempSum = 0;
                    tempCount = 0;
                }

                JSONObject mainObj = forecast.getJSONObject("main");
                double temp = mainObj.getDouble("temp");
                tempSum += temp;
                tempCount++;

                String icon = "";
                JSONArray weatherArray = forecast.getJSONArray("weather");
                if (weatherArray.length() > 0) {
                    icon = weatherArray.getJSONObject(0).getString("icon");
                }

                currentDayHours.add(new HourlyForecast(timestamp, temp, icon));
                lastDate = date;
            }

            // Добавляем последний день
            if (!currentDayHours.isEmpty()) {
                dailyHours.add(new ArrayList<>(currentDayHours));
                dailyDates.add(currentDayHours.get(0).getTimestamp());
                dailyTemps.add(tempSum / tempCount);
            }

            // Создаем объекты DailyForecast
            for (int i = 0; i < dailyHours.size() && i < 7; i++) { // Ограничиваем 7 днями
                dailyForecasts.add(new DailyForecast(dailyDates.get(i), dailyTemps.get(i), dailyHours.get(i)));
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }

        return dailyForecasts;
    }
}