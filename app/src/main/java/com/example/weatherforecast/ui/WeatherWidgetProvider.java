package com.example.weatherforecast.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.repository.WeatherRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherWidgetProvider extends AppWidgetProvider {
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";
    private static final String TAG = "WeatherWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "Starting widget update for IDs: " + java.util.Arrays.toString(appWidgetIds));

        // Создаем пул потоков для асинхронных операций
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Handler для обновления UI на главном потоке
        Handler mainHandler = new Handler(Looper.getMainLooper());

        for (int appWidgetId : appWidgetIds) {
            // Инициализируем RemoteViews
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.temperature, "Загрузка...");
            views.setTextViewText(R.id.condition, "");
            views.setImageViewResource(R.id.weather, R.drawable.weather);
            appWidgetManager.updateAppWidget(appWidgetId, views);

            executor.execute(() -> {
                WeatherRepository weatherRepository = new WeatherRepository(context.getApplicationContext());
                Location location = weatherRepository.getLatestLocation();
                String city = (location != null && location.getCityName() != null && !location.getCityName().isEmpty())
                        ? location.getCityName() : "Moscow";
                Log.d(TAG, "Selected city from DB: " + city);

                // Выполняем API-запрос на главном потоке
                mainHandler.post(() -> {
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";
                    Log.d(TAG, "API URL: " + url);

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            response -> {
                                Log.d(TAG, "API response received: " + response.toString());
                                parseCurrentWeatherResponse(context, response, appWidgetId, city);
                            },
                            error -> {
                                Log.e(TAG, "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), error);
                                RemoteViews errorViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                                errorViews.setTextViewText(R.id.temperature, "Ошибка API");
                                errorViews.setTextViewText(R.id.condition, "Проверьте интернет");
                                errorViews.setImageViewResource(R.id.weather, R.drawable.weather);
                                appWidgetManager.updateAppWidget(appWidgetId, errorViews);
                            });

                    RequestQueue queue = Volley.newRequestQueue(context);
                    queue.add(request);
                });
            });
        }

        // Закрываем пул потоков
        executor.shutdown();
    }

    private void parseCurrentWeatherResponse(Context context, JSONObject response, int appWidgetId, String city) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        try {
            if (!response.has("main")) {
                Log.e(TAG, "No 'main' object in response");
                views.setTextViewText(R.id.temperature, "Ошибка данных");
                views.setTextViewText(R.id.condition, "");
                views.setImageViewResource(R.id.weather, R.drawable.weather);
                appWidgetManager.updateAppWidget(appWidgetId, views);
                return;
            }

            JSONObject mainObj = response.getJSONObject("main");
            Log.d(TAG, "Main object: " + mainObj.toString());

            int temp = (int) Math.round(mainObj.getDouble("temp"));
            int feelsLike = (int) Math.round(mainObj.getDouble("feels_like"));
            Log.d(TAG, "Parsed temp: " + temp + ", feels like: " + feelsLike);

            views.setTextViewText(R.id.temperature, temp + "°C");
            views.setTextViewText(R.id.condition, "Ощущается: " + feelsLike + "°C");

            if (response.has("weather") && response.getJSONArray("weather").length() > 0) {
                JSONArray weatherArray = response.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                String icon = weatherObj.getString("icon");
                Log.d(TAG, "Weather icon code: " + icon);

                int iconResId = getIconResource(icon);
                if (iconResId != 0) {
                    views.setImageViewResource(R.id.weather, iconResId);
                    Log.d(TAG, "Icon resource set: " + iconResId);
                } else {
                    Log.w(TAG, "No resource for icon: " + icon);
                    views.setImageViewResource(R.id.weather, R.drawable.weather);
                }
            } else {
                Log.w(TAG, "No weather data in response");
                views.setImageViewResource(R.id.weather, R.drawable.weather);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d(TAG, "Widget updated successfully for city: " + city);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
            views.setTextViewText(R.id.temperature, "Ошибка парсинга");
            views.setTextViewText(R.id.condition, "");
            views.setImageViewResource(R.id.weather, R.drawable.weather);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getIconResource(String icon) {
        Log.d(TAG, "Getting resource for icon: " + icon);
        switch (icon) {
            case "01d": return R.drawable.clear_day;
            case "01n": return R.drawable.clear_night;
            case "02d": return R.drawable.clouds;
            case "02n": return R.drawable.few_clouds_night;
            case "03d":
            case "03n": return R.drawable.scattered_clouds;
            case "04d":
            case "04n": return R.drawable.clouds;
            case "09d":
            case "09n": return R.drawable.shower_rain;
            case "10d":
            case "10n": return R.drawable.rain;
            case "11d":
            case "11n": return R.drawable.thunderstorm;
            case "13d":
            case "13n": return R.drawable.snow;
            case "50d":
            case "50n": return R.drawable.mist;
            default:
                Log.w(TAG, "Unknown icon code: " + icon);
                return 0;
        }
    }
}