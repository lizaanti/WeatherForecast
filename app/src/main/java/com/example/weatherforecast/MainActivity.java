package com.example.weatherforecast;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecast.R;
import com.example.weatherforecast.Weather;
import com.example.weatherforecast.WeatherAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView cityName;
    private TextView tempResult, feels;
    private SearchView searchView;
    private RelativeLayout home;
    private ImageView ivBack, viewIcon;
    private RecyclerView weatherRV;
    private Button toKnowButton;
    private ArrayList<Weather> weatherArrayList = new ArrayList<>();
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressBar.setVisibility(View.GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        searchView = findViewById(R.id.SearchView);
        tempResult = findViewById(R.id.Temperature);
        toKnowButton = findViewById(R.id.toKnowWeather);
        progressBar = findViewById(R.id.Loading);
        cityName = findViewById(R.id.CityNameTV);
        feels = findViewById(R.id.ConditionTV);
        home = findViewById(R.id.HomePage);
        ivBack = findViewById(R.id.IVBack);
        viewIcon = findViewById(R.id.ViewIcon);
        weatherRV = findViewById(R.id.RVWeather);
        weatherAdapter = new WeatherAdapter(this, weatherArrayList);
        weatherRV.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешения
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName.setText(getCityName(location.getLongitude(), location.getLatitude()));

        /*feels = findViewById(R.id.feels);
        tempMax = findViewById(R.id.temp_max);
        tempMin = findViewById(R.id.temp_min);
        pressure = findViewById(R.id.pressure);
        humidity = findViewById(R.id.humidity);*/

    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult){
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        if(requestCode == PERMISSION_CODE){
            if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Разрешение предоставлено..", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Пожалуйста, предоставьте разрешение", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private String getCityName(double longitude, double latitude){
        String cityNameStr = "Не найдено";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses =  gcd.getFromLocation(latitude, longitude, 10);

            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityNameStr = city;
                        break; // Прерываем цикл после нахождения города
                    }
                }
            }
            if (cityNameStr.equals("Не найдено")) {
                Log.d("TAG", "ГОРОД НЕ НАЙДЕН");
                Toast.makeText(this, "Город пользователя не найден..", Toast.LENGTH_SHORT).show();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityNameStr;
    }

    private void getWeatherIngo(String cityNameStr) {

        progressBar.setVisibility(View.VISIBLE);
        cityName.setText(cityNameStr);
        cityNameStr = searchView.getQuery().toString();
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityNameStr + "&appid=355be3e73060ee9814fdfbee14e40a1a&units=metric&lang=ru";

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        toKnowButton.setOnClickListener(view -> {
            if (searchView.getQuery().toString().trim().equals("")) {
                Toast.makeText(MainActivity.this, R.string.if_no_input, Toast.LENGTH_LONG).show();
            } else {
                String city = searchView.getQuery().toString();
                String searchUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=355be3e73060ee9814fdfbee14e40a1a&units=metric&lang=ru";
                new GetURL().execute(searchUrl); // используем URL для поиска
            }
        });

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);
                        home.setVisibility(View.VISIBLE);
                        weatherArrayList.clear();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Обработка ошибки
                        Toast.makeText(MainActivity.this, "Корректно введите название города..", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Добавляем запрос в очередь для выполнения
        requestQueue.add(jsonObjectRequest);
    }

    private class GetURL extends AsyncTask<String, String, String>{

        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]); //открываем URl соединение
                connection = (HttpURLConnection) url.openConnection(); //открываем http соединение
                connection.connect();

                InputStream input = connection.getInputStream(); //считываем весь поток, который получили
                reader = new BufferedReader(new InputStreamReader(input));

                StringBuffer buffer = new StringBuffer();
                String str = "";

                while((str = reader.readLine()) != null){
                    buffer.append(str).append("\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                if(connection != null)
                    connection.disconnect();

                try {
                    if (reader != null)
                        reader.close();
                }
                catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject weatherInfo = jsonObj.getJSONObject("main");
                String temp = weatherInfo.getString("temp");
                String feelsLike = weatherInfo.getString("feels_like");
                /*String temp_max = weatherInfo.getString("temp_max");
                String temp_min = weatherInfo.getString("temp_min");
                String pressure1 = weatherInfo.getString("pressure");
                String humidity1 = weatherInfo.getString("humidity");*/

                Double.parseDouble(temp);
                long tempRes = Math.round(Float.parseFloat(temp));

                tempResult.setText(Long.toString(tempRes) + "°");

                Double.parseDouble(feelsLike);
                long FeelsRes = Math.round(Float.parseFloat(feelsLike));
                feels.setText("Ощущается как: " + feelsLike + "°");
                /*tempMax.setText("Максимальная температура: " + temp_max);
                tempMin.setText("Минимальная температура: " +temp_min);
                pressure.setText("Давление: " + pressure1);
                humidity.setText("Влажность: " + humidity1);*/
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}