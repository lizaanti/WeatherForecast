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

    @ColumnInfo(name = "new_field")
    public String newField;

    public void setNewField(String newField) {
        this.newField = newField;
    }

    public String getNewField() {
        return newField;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}
