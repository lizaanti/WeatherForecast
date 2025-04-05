package com.example.weatherforecast.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.weatherforecast.data.entities.WeatherData;

@Dao
public interface WeatherDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeatherData weatherData);

    @Transaction
    @Query("SELECT * FROM weather_data WHERE location_id = :locationId ORDER BY timestamp DESC LIMIT 1")
    LiveData<WeatherData> getLatestWeather(int locationId);
}

