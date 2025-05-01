package com.example.weatherforecast.data.dao;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weatherforecast.data.entities.User;

@Dao
public interface UserDao {
    @Insert(onConflict = REPLACE)
    Long insert(User u);
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);
    @Update
    void update(User u);
}
