package com.example.weatherforecast.data.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weatherforecast.data.entities.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(com.example.weatherforecast.data.entities.Location location);

    @Query("SELECT * FROM locations")
    LiveData<List<com.example.weatherforecast.data.entities.Location>> getAllLocations();

    @Query("SELECT * FROM locations WHERE city_name = :cityName LIMIT 1")
    Location findByName(String cityName);

    @Delete
    void delete(com.example.weatherforecast.data.entities.Location location);

    @Query("SELECT * FROM locations ORDER BY id DESC LIMIT 1")
    Location getLatestLocation();
}
