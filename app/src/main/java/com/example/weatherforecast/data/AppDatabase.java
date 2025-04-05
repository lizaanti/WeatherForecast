package com.example.weatherforecast.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.weatherforecast.data.dao.ForecastDao;
import com.example.weatherforecast.data.dao.LocationDao;
import com.example.weatherforecast.data.dao.UserPreferencesDao;
import com.example.weatherforecast.data.dao.WeatherDataDao;
import com.example.weatherforecast.data.entities.Forecast;
import com.example.weatherforecast.data.entities.Location;
import com.example.weatherforecast.data.entities.UserPreferences;
import com.example.weatherforecast.data.entities.WeatherData;

@Database(entities = {UserPreferences.class, Location.class, WeatherData.class, Forecast.class},
        version = 2,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserPreferencesDao userPreferencesDao();
    public abstract LocationDao locationDao();
    public abstract WeatherDataDao weatherDataDao();
    public abstract ForecastDao forecastDao();

    private static volatile AppDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE user_preferences ADD COLUMN update_interval INTEGER NOT NULL DEFAULT 60");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "weather_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
