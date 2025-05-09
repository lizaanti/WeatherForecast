package com.example.weatherforecast.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.DailyForecast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyForecastAdapter extends RecyclerView.Adapter<WeeklyForecastAdapter.ViewHolder> {
    private List<DailyForecast> dailyForecasts = new ArrayList<>();
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MM", Locale.getDefault());

    public WeeklyForecastAdapter(Context context){
        this.context = context;
    }

    public void setDailyForecasts(List<DailyForecast> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyForecast dailyForecast = dailyForecasts.get(position);

        // Устанавливаем дату
        String date = dateFormat.format(new Date(dailyForecast.getDate()));
        holder.dateTextView.setText(date);

        // Устанавливаем среднюю температуру
        int avgTemp = (int) Math.round(dailyForecast.getAvgTemp());
        holder.tempTextView.setText(avgTemp + "°C");

        // Настраиваем вложенный RecyclerView для почасового прогноза
        HourlyForecastAdapter hourlyAdapter = new HourlyForecastAdapter(context);
        holder.hourlyRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.hourlyRecyclerView.setAdapter(hourlyAdapter);
        hourlyAdapter.setForecasts(dailyForecast.getHourlyForecasts());
    }

    @Override
    public int getItemCount() {
        return dailyForecasts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView tempTextView;
        RecyclerView hourlyRecyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.day_date);
            tempTextView = itemView.findViewById(R.id.day_temp);
            hourlyRecyclerView = itemView.findViewById(R.id.hourly_forecast_recycler);
        }
    }
}
