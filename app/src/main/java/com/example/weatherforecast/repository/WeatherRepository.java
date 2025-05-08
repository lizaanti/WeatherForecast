package com.example.weatherforecast.repository;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import com.example.weatherforecast.data.AppDatabase;
import com.example.weatherforecast.data.dao.LocationDao;
import com.example.weatherforecast.data.entities.Forecast;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.data.entities.UserPreferences;
import com.example.weatherforecast.data.entities.WeatherData;

import java.util.ArrayList;
import java.util.List;

public class WeatherRepository {
    private final AppDatabase database;
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

    /*public LiveData<List<UserPreferences>> getAllPreferences() {
        return database.userPreferencesDao().getAllPreferences();
    }*/


    public void insertWeatherData(WeatherData weatherData, String cityName) {
        new Thread(() -> {
            LocationDao locDao = database.locationDao();
            Location loc = locDao.findByName(cityName);
            if(loc == null){
                loc = new Location();
                loc.setCityName(cityName);
                long newId = locDao.insert(loc);
                loc.setId((int)newId);
            }
            weatherData.setLocationId(loc.getId());
            database.weatherDataDao().insert(weatherData);
        }).start();
    }

    public void insertLocation(Location location) {
        new Thread(() -> {
            try{
                database.locationDao().insert(location);
            }catch (Exception e){
                Log.e(TAG, "Error insert forecasts.", e);
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

    public WeatherData getMostRecentWeatherSync() {
        return database.weatherDataDao().getMostRecentWeatherSync();
    }

    public String getCurrentConditionString() {
        WeatherData wd = getMostRecentWeatherSync();
        if (wd == null) {
            return "SUNNY";
        }
        String icon = wd.getWeatherIcon();
        if (icon.endsWith("n")) {
            return "NIGHT";
        }

        if (icon.startsWith("01")) {
            return "SUNNY";
        }
        if (icon.startsWith("13")) {
            return "SNOW";
        }
        return "RAIN";
    }
}

