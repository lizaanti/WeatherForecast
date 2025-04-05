package com.example.weatherforecast.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weatherforecast.data.entities.UserPreferences;

@Dao
public interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserPreferences preferences);

    @Query("SELECT * FROM user_preferences LIMIT 1")
    UserPreferences getPreferences();

    @Update
    void update(UserPreferences preferences);
}
