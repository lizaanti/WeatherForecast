package com.example.weatherforecast.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.adapter.WeatherAdapter;
import com.example.weatherforecast.model.Weather;

import java.util.ArrayList;

public class ForecastDetailActivity extends AppCompatActivity {

    private RecyclerView detailRecyclerView;
    private ArrayList<Weather> detailedForecastList;
    private WeatherAdapter weatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_detail);

        detailRecyclerView = findViewById(R.id.detailRecyclerView);
        // Получаем список прогнозов из Intent
        detailedForecastList = (ArrayList<Weather>) getIntent().getSerializableExtra("forecastList");
        if(detailedForecastList == null){
            detailedForecastList = new ArrayList<>();
        }
        weatherAdapter = new WeatherAdapter(this, detailedForecastList);
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailRecyclerView.setAdapter(weatherAdapter);
    }
}
