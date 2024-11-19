package com.example.weatherforecast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
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

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        Weather modal = weatherArrayList.get(position);
        holder.temperatureTV.setText(modal.getTemperature() + "℃");
        Picasso.get().load("http:".concat(modal.getIcon())).into(holder.conditionTV);
        holder.feelsTV.setText(modal.getFeelsLike() + "℃");
        SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat outputDate = new SimpleDateFormat("hh:mm aa");
        try{
            Date t = inputDate.parse(modal.getTime());
            holder.timeTV.setText(outputDate.format(t));
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView feelsTV, temperatureTV, timeTV;
        private ImageView conditionTV;
        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            feelsTV = itemView.findViewById(R.id.FeelsLikeTV);
            temperatureTV = itemView.findViewById(R.id.Temperature);
            timeTV = itemView.findViewById(R.id.TimeTV);
            conditionTV = itemView.findViewById(R.id.ConditionTV);
        }
    }
}
