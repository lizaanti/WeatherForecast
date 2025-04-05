package com.example.weatherforecast.data.entities;

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "forecasts",
        foreignKeys = @ForeignKey(entity = WeatherData.class,
                parentColumns = "id",
                childColumns = "weather_data_id",
                onDelete = ForeignKey.CASCADE))
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
}
