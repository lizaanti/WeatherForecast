package com.example.weatherforecast.data.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import androidx.core.app.NotificationCompat;

import com.example.weatherforecast.R;
import com.example.weatherforecast.ui.ForecastDetailActivity;
import com.example.weatherforecast.ui.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseService extends FirebaseMessagingService {
    private static final String PREFS_NAME = "fcm_prefs";
    private static final String KEY_TOKEN = "messaging_token";
    public static final String CHANNEL_ID = "weather_notifications";

    // Сохранение токена в SharedPreferences
    private void saveToken(String token){
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }
    // При необходимости получаем последний токен
    public static String getSavedToken(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message){
        super.onMessageReceived(message);

        // Разбор данных сообщения
        boolean alert = "true".equals(message.getData().get("alert"));
        Intent intent;
        if (alert){
            String contextType = message.getData().get("context");
            String contextData = message.getData().get("contextData");

            // Обращение к Activity
            switch (contextType){
                case "weatherToday":
                    intent = new Intent(this, MainActivity.class);
                    break;
                case "weatherForAWeek":
                    intent = new Intent(this, ForecastDetailActivity.class);
                    break;
                default:
                    intent = new Intent(this, MainActivity.class);
            }
            showNotification(message, intent);
        }
        else{
            showNotification(message, null);
        }
    }

    private void showNotification(RemoteMessage message, Intent intent) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt();

        // Флаг для обновления Intent
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(
                    this, 0, intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            ? PendingIntent.FLAG_IMMUTABLE
                            : PendingIntent.FLAG_ONE_SHOT
            );


            // Создаем канал
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(nm);
            }

            // Тело непосредственно самого уведомления
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.getData().getOrDefault("title", message.getNotification() != null ? message.getNotification().getTitle() : ""))
                    .setContentText(message.getData().getOrDefault("body", message.getNotification() != null ? message.getNotification().getBody() : ""))
                    .setSmallIcon(R.drawable.weather)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            nm.notify(notificationId, builder.build());
        }
        else{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.getData().getOrDefault("title", message.getNotification() != null ? message.getNotification().getTitle() : ""))
                    .setContentText(message.getData().getOrDefault("body", message.getNotification() != null ? message.getNotification().getBody() : ""))
                    .setSmallIcon(R.drawable.weather)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            nm.notify(notificationId, builder.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager nm){
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Weather Forecast Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription("Уведомление о погодном прогнозе");
        channel.enableLights(true);
        channel.setLightColor(Color.CYAN);
        nm.createNotificationChannel(channel);
    }
}
