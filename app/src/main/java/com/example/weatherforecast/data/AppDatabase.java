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
        version = 3,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserPreferencesDao userPreferencesDao();
    public abstract LocationDao locationDao();
    public abstract WeatherDataDao weatherDataDao();
    public abstract ForecastDao forecastDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE weather_data ADD COLUMN new_field TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE weather_data ADD COLUMN cityName TEXT");
            database.execSQL("ALTER TABLE weather_data ADD COLUMN windSpeed REAL NOT NULL DEFAULT 0");
            database.execSQL("DROP INDEX IF EXISTS index_weather_data_location_id");
            database.execSQL("CREATE INDEX index_weather_data_location_id ON weather_data(location_id)");
        }
    };
}