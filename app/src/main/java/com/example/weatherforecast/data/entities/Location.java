package com.example.weatherforecast.data.entities;

import android.support.annotation.NonNull;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class Location {
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "city_name")
    public String cityName;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    public Location(@NonNull String cityName, Double latitude, Double longitude, boolean isFavorite) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFavorite = isFavorite;
    }

    // Пустой конструктор для Room
    public Location() {}

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

}
