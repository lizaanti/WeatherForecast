package com.example.weatherforecast.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.repository.WeatherRepository;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherWidgetProvider extends AppWidgetProvider {
    private static final String API_KEY = "355be3e73060ee9814fdfbee14e40a1a";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WeatherRepository weatherRepository = new WeatherRepository(context.getApplicationContext());
            Location location = weatherRepository.getLatestLocation();
            int locationId = (location != null) ? location.getId() : 1;

            String city = (location != null) ? location.getCityName() : "Moscow";
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> parseCurrentWeatherResponse(context, response, appWidgetId, city),
                    error -> {
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                        views.setTextViewText(R.id.temperature, "Ошибка данных");
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    });
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        }
    }

    private void parseCurrentWeatherResponse(Context context, JSONObject response, int appWidgetId, String city) {
        try {
            JSONObject mainObj = response.getJSONObject("main");
            int temp = (int) Math.round(mainObj.getDouble("temp"));
            int feelsLike = (int) Math.round(mainObj.getDouble("feels_like"));
            int humidity = mainObj.getInt("humidity");
            int pressure = mainObj.getInt("pressure");
            JSONObject windObj = response.getJSONObject("wind");
            double windSpeed = windObj.getDouble("speed");

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.temperature, temp + "°C");
            views.setTextViewText(R.id.condition, "Ощущается: " + feelsLike + "°C");


            JSONArray weatherArray = response.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                String icon = weatherObj.getString("icon");
                int iconResId = getIconResource(icon);
                if (iconResId != 0) {
                    views.setImageViewResource(R.id.weather, iconResId);
                } else {
                    views.setImageViewResource(R.id.weather, R.drawable.weather);
                }
            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (JSONException e) {
            e.printStackTrace();
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.temperature, "Ошибка получения данных");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getIconResource(String icon) {
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
            default: return 0;
        }
    }
}