package com.example.weatherforecast.data.dao;


import android.location.Location;

import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Location location);

    @Query("SELECT * FROM locations WHERE is_favorite = 1")
    LiveData<List<Location>> getFavoriteLocations();

    @Delete
    void delete(Location location);
}
