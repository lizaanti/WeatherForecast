package com.example.weatherforecast.data.entities;

import android.support.annotation.NonNull;
import android.widget.ImageView;

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
        indices = @Index("location_id"))
public class WeatherData {

    @PrimaryKey(autoGenerate = true)
    public Integer id;

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

    @ColumnInfo(name = "cityName")
    @NonNull
    public String cityName;

    @ColumnInfo(name = "windSpeed")
    public double windSpeed;


    public WeatherData() {

    }

    public WeatherData(int id, int locationId, double temperature, double humidity, int pressure, String weatherIcon, double windSpeed,  @NonNull String cityName, long timestamp, String newField) {
        this.id = id;
        this.locationId = locationId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.weatherIcon = weatherIcon;
        this.windSpeed = windSpeed;
        this.cityName = cityName;
        this.timestamp = timestamp;
        this.newField = newField;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }

    public String getWeatherIcon() { return weatherIcon; }
    public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getNewField() { return newField; }
    public void setNewField(String newField) { this.newField = newField; }

    @androidx.annotation.NonNull
    public String getCityName() { return cityName; }

    public void setCityName(@androidx.annotation.NonNull String cityName) {
        this.cityName = cityName;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
}