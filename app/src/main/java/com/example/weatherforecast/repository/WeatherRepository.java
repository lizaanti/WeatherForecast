package com.example.weatherforecast.repository;

public class WeatherRepository {
    private AppDatabase database;

    public WeatherRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public LiveData<WeatherData> getLatestWeather(int locationId) {
        return database.weatherDataDao().getLatestWeather(locationId);
    }

}
