package com.example.weatherforecast.repository;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import com.example.weatherforecast.data.AppDatabase;
import com.example.weatherforecast.data.entities.Forecast;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.data.entities.UserPreferences;
import com.example.weatherforecast.data.entities.WeatherData;

import java.util.ArrayList;
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


    public void insertWeatherData(WeatherData weatherData, String cityName) {
        new Thread(() -> {
            Location location = database.locationDao().getLocationByCityName(cityName);
            if(location == null){
                location = new Location();
                location.setCityName(cityName);
                database.locationDao().insert(location);
                location = database.locationDao().getLocationByCityName(cityName);
            }
            if(location != null){
                weatherData.setLocationId(location.getId());
                weatherData.setTimestamp(System.currentTimeMillis());
                database.weatherDataDao().insert(weatherData);
            }
        }).start();
    }

    public void insertForecasts(List<Forecast> forecasts) {
        if(forecasts == null){
            Log.e(TAG, "Forecast list is null.");
            return;
        }
        List<Forecast> validForecast = new ArrayList<>();
        for(Forecast f : forecasts){
            if(f != null){
                validForecast.add(f);
            }
        }
        if(validForecast.isEmpty()){
            Log.w(TAG, "No valid objects.");
            return;
        }
        new Thread(() -> {
            try{
                database.forecastDao().insertAll(validForecast);
            }catch (Exception e){
                Log.e(TAG, "Error insert forecasts.", e);
            }
        }).start();
    }

    public LiveData<WeatherData> getMostRecentWeather() {
        return database.weatherDataDao().getMostRecentWeather();
    }

    public LiveData<List<Forecast>> getForecastsByWeatherDataId(int weatherDataId){
        return database.forecastDao().getForecastsByWeatherDataId(weatherDataId);
    }

}