package com.example.weatherforecast.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.weatherforecast.data.AppDatabase;
import com.example.weatherforecast.data.entities.Forecast;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.data.entities.UserPreferences;
import com.example.weatherforecast.data.entities.WeatherData;

import java.util.List;

public class WeatherRepository {
    private AppDatabase database;
    private static final String TAG = "WeatherRepository";

    public WeatherRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public LiveData<WeatherData> getLatestWeather(int locationId) {
        return database.weatherDataDao().getLatestWeather(locationId);
    }

    public LiveData<List<Location>> getAllLocations() {
        return database.locationDao().getAllLocations();
    }

    public LiveData<List<WeatherData>> getAllWeatherData() {
        return database.weatherDataDao().getAllWeatherData();
    }

    public LiveData<List<Forecast>> getAllForecasts() {
        return database.forecastDao().getAllForecasts();
    }

    public LiveData<List<UserPreferences>> getAllPreferences() {
        return database.userPreferencesDao().getAllPreferences();
    }

    // Метод для добавления тестовых данных (если таблицы пустые)
    public void insertTestData() {
        new Thread(() -> {
            // Добавляем тестовое местоположение
            Location location = new Location();
            location.setCityName("Test City");
            location.setLatitude(55.7558);
            location.setLongitude(37.6173);
            location.setFavorite(true);
            database.locationDao().insert(location);
            Log.d(TAG, "Inserted test location: " + location.getCityName());

            // Добавляем тестовые данные о погоде
            WeatherData weatherData = new WeatherData();
            weatherData.setLocationId(1); // Предполагаем, что locationId = 1
            weatherData.setTemperature(20.5);
            weatherData.setHumidity(65.0);
            weatherData.setPressure(1013);
            weatherData.setWeatherIcon("01d");
            weatherData.setTimestamp(System.currentTimeMillis());
            database.weatherDataDao().insert(weatherData);
            Log.d(TAG, "Inserted test weather data: " + weatherData.getTemperature());

            // Добавляем тестовый прогноз
            Forecast forecast = new Forecast();
            forecast.setWeatherDataId(1); // Предполагаем, что weatherDataId = 1
            forecast.setDateTime("2025-04-08 12:00:00");
            forecast.setTemperature(22.0);
            forecast.setFeelsLike(21.5);
            forecast.setWeatherIcon("02d");
            database.forecastDao().insert(forecast);
            Log.d(TAG, "Inserted test forecast: " + forecast.getDateTime());

            // Добавляем тестовые настройки
            UserPreferences preferences = new UserPreferences();
            preferences.setTemperatureUnit("C");
            preferences.setWindSpeedUnit("m/s");
            preferences.setNotificationsEnabled(true);
            database.userPreferencesDao().insert(preferences);
            Log.d(TAG, "Inserted test preferences: " + preferences.getTemperatureUnit());
        }).start();
    }
}