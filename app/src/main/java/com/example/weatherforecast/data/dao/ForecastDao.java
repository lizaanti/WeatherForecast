package com.example.weatherforecast.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weatherforecast.data.entities.Forecast;

import java.util.List;

@Dao
public interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Forecast forecast);

    @Query("SELECT * FROM forecasts WHERE weather_data_id = :weatherDataId")
    LiveData<List<Forecast>> getForecasts(int weatherDataId);
}
