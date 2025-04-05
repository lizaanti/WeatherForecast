package com.example.weatherforecast.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforecast.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_TEMP_UNIT = "temp_unit"; // "C" или "F"
    private Switch unitSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        unitSwitch = findViewById(R.id.switchTempUnit);

        // Загружаем сохранённое значение единицы (по умолчанию "C")
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String unit = prefs.getString(KEY_TEMP_UNIT, "C");
        unitSwitch.setChecked(unit.equals("F"));

        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Сохраняем выбор пользователя
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_TEMP_UNIT, isChecked ? "F" : "C");
                editor.apply();
            }
        });
    }
}
