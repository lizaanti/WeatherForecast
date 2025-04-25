package com.example.weatherforecast.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true) public int id;
    @ColumnInfo(name="email") public String email;
    @ColumnInfo(name="isConfirmed") public boolean isConfirmed;
    @ColumnInfo(name="name") public String name;
    @ColumnInfo(name="avatarUri") public String avatarUri;
    @ColumnInfo(name="homeCity") public String homeCity;
}
