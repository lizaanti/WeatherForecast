package com.example.weatherforecast.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "forecasts",
indices = {@Index("weather_data_id")})
public class Forecast {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "weather_data_id")
    public int weatherDataId;

    @ColumnInfo(name = "date_time")
    public String dateTime;

    @ColumnInfo(name = "temperature")
    public double temperature;

    @ColumnInfo(name = "feels_like")
    public double feelsLike;

    @ColumnInfo(name = "weather_icon")
    public String weatherIcon;

    public void setId(int id) {
        this.id = id;
    }

    public void setWeatherDataId(int weatherDataId) {
        this.weatherDataId = weatherDataId;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public int getId() {
        return id;
    }

    public int getWeatherDataId() {
        return weatherDataId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

}

