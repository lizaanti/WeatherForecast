package com.example.weatherforecast.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.model.Weather;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Weather> weatherArrayList;

    public WeatherAdapter(Context context, ArrayList<Weather> weatherArrayList) {
        this.context = context;
        this.weatherArrayList = weatherArrayList;
    }

    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item, parent, false);
        return new ViewHolder(view);
      
    }

    @Override
    public void onBindViewHolder(WeatherAdapter.ViewHolder holder, int position) {
        Weather modal = weatherArrayList.get(position);
        holder.temperatureTV.setText(modal.getTemperature() + "℃");
        holder.feelsTV.setText("Ощущается: " + modal.getFeelsLike() + "℃");
        // Преобразуем время, если нужно:
        SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputDate = new SimpleDateFormat("dd MMM");
        try {
            Date t = inputDate.parse(modal.getTime());
            holder.timeTV.setText(outputDate.format(t));
        } catch (ParseException e) {
            holder.timeTV.setText(modal.getTime());
        }
        // Загружаем иконку через Picasso
        if(modal.getIcon() != null && !modal.getIcon().isEmpty()){
            Picasso.get()
                    .load(modal.getIcon())
                    .placeholder(R.drawable.weather) // placeholder на случай ошибки
                    .into(holder.conditionIV);
        }
    }

    @Override
    public int getItemCount() {
        return weatherArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView feelsTV, temperatureTV, timeTV;
        private ImageView conditionIV;
        public ViewHolder(View itemView) {
            super(itemView);
            feelsTV = itemView.findViewById(R.id.FeelsLikeTV);
            temperatureTV = itemView.findViewById(R.id.TemperatureTV);
            timeTV = itemView.findViewById(R.id.TimeTV);
            conditionIV = itemView.findViewById(R.id.IVCondition);
        }
    }
}
