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
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNewField() {
        return newField;
    }

    public void setNewField(String newField) {
        this.newField = newField;
    }

    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "location_id")
    public Integer locationId;

    @ColumnInfo(name = "temperature")
    public Double temperature;

    @ColumnInfo(name = "humidity")
    public Double humidity;

    @ColumnInfo(name = "pressure")
    public Integer pressure;

    @ColumnInfo(name = "weather_icon")
    public String weatherIcon;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "new_field")
    public String newField;

}
