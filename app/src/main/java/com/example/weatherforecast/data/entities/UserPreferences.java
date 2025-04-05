package com.example.weatherforecast.data.entities;
import androidx.room.Entity;

@Entity(tableName = "user_preferences")
public class UserPreferences {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "temperature_unit")
    public String temperatureUnit;

    @ColumnInfo(name = "wind_speed_unit")
    public String windSpeedUnit;

    @ColumnInfo(name = "notifications_enabled")
    public boolean notificationsEnabled;
}
