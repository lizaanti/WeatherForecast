package com.example.weatherforecast.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "forecasts",
        foreignKeys = @ForeignKey(entity = WeatherData.class,
                parentColumns = "id",
                childColumns = "weather_data_id",
                onDelete = ForeignKey.CASCADE),
indices = {@Index("weather_data_id")})
public class Forecast {

    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "weather_data_id")
    public Integer weatherDataId;

    @ColumnInfo(name = "date_time")
    public String dateTime;

    @ColumnInfo(name = "temperature")
    public Double temperature;

    @ColumnInfo(name = "feels_like")
    public Double feelsLike;

    @ColumnInfo(name = "weather_icon")
    public String weatherIcon;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setWeatherDataId(Integer weatherDataId) {
        this.weatherDataId = weatherDataId;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setFeelsLike(Double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public Integer getId() {
        return id;
    }

    public Integer getWeatherDataId() {
        return weatherDataId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getFeelsLike() {
        return feelsLike;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

}

