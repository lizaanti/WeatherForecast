package com.example.weatherforecast.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.HourlyForecast;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
    private List<HourlyForecast> forecasts = new ArrayList<>();
    private Context context;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public HourlyForecastAdapter(Context context) {
        this.context = context;
    }

    public void setForecasts(List<HourlyForecast> forecasts) {
        this.forecasts = forecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyForecast forecast = forecasts.get(position);

        // Устанавливаем время
        String time = timeFormat.format(new Date(forecast.getTimestamp()));
        holder.timeTextView.setText(time);

        // Устанавливаем температуру
        int temp = (int) Math.round(forecast.getTemperature());
        holder.tempTextView.setText(temp + "°C");

        // Устанавливаем иконку
        int iconResId = getIconResource(forecast.getIcon());
        holder.iconImageView.setImageResource(iconResId != 0 ? iconResId : R.drawable.weather);
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        ImageView iconImageView;
        TextView tempTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.hour_time);
            iconImageView = itemView.findViewById(R.id.hour_icon);
            tempTextView = itemView.findViewById(R.id.hour_temp);
        }
    }

    private int getIconResource(String icon) {
        switch (icon) {
            case "01d": return R.drawable.clear_day;
            case "01n": return R.drawable.clear_night;
            case "02d": return R.drawable.clouds;
            case "02n": return R.drawable.few_clouds_night;
            case "03d":
            case "03n": return R.drawable.scattered_clouds;
            case "04d":
            case "04n": return R.drawable.clouds;
            case "09d":
            case "09n": return R.drawable.shower_rain;
            case "10d":
            case "10n": return R.drawable.rain;
            case "11d":
            case "11n": return R.drawable.thunderstorm;
            case "13d":
            case "13n": return R.drawable.snow;
            case "50d":
            case "50n": return R.drawable.mist;
            default: return 0;
        }
    }
}