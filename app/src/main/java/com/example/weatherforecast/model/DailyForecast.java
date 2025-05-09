package com.example.weatherforecast.model;

import com.example.weatherforecast.ui.HourlyForecast;

import java.util.List;

public class DailyForecast {
    private long date;
    private double avgTemp;
    private List<HourlyForecast> hourlyForecasts;


    public DailyForecast(long date, double avgTemp, List<HourlyForecast> hourlyForecasts) {
        this.date = date;
        this.avgTemp = avgTemp;
        this.hourlyForecasts = hourlyForecasts;
    }

    public long getDate() {
        return date;
    }

    public double getAvgTemp() {
        return avgTemp;
    }

    public List<HourlyForecast> getHourlyForecasts() {
        return hourlyForecasts;
    }
}
