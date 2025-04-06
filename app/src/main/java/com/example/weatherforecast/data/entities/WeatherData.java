package com.example.weatherforecast.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_data",
foreignKeys = @ForeignKey(entity = Location.class,
parentColumns = "id",
childColumns = "location_id",
onDelete = ForeignKey.CASCADE),
indices = {@Index("location_id")})
public class WeatherData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "location_id")
    public int locationId;

    @ColumnInfo(name = "temperature")
    public double temperature;

    @ColumnInfo(name = "humidity")
    public double humidity;

    @ColumnInfo(name = "pressure")
    public int pressure;

    @ColumnInfo(name = "weather_icon")
    public String weatherIcon;

    @ColumnInfo(name = "timestamp")
    public long timestamp;


}
