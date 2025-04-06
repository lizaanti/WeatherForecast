package com.example.weatherforecast.data.entities;

import android.support.annotation.NonNull;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class Location {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "city_name")
    public String cityName;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    public Location(@NonNull String cityName, double latitude, double longitude, boolean isFavorite) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFavorite = isFavorite;
    }

    // Пустой конструктор для Room
    public Location() {}

}
