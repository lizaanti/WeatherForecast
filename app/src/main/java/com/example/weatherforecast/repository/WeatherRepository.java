package com.example.weatherforecast.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.weatherforecast.data.AppDatabase;
import com.example.weatherforecast.data.entities.WeatherData;

public class WeatherRepository {
    private AppDatabase database;

    public WeatherRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public LiveData<WeatherData> getLatestWeather(int locationId) {
        return database.weatherDataDao().getLatestWeather(locationId);
    }

}
