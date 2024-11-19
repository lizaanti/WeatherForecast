package com.example.weatherforecast;

public class Weather {
    private String time;
    private String temperature;
    private String icon;
    private String feelsLike;

    public Weather(String time, String temperature, String icon, String feelsLike) {
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
        this.feelsLike = feelsLike;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }
}
