package com.example.weatherforecast.ui;

public class HourlyForecast {
    private long timestamp;
    private double temperature;
    private String icon;

    public HourlyForecast(long timestamp, double temperature, String icon) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.icon = icon;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }
}
