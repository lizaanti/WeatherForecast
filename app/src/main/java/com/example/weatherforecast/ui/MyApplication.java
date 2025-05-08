package com.example.weatherforecast.ui;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MapKitFactory.setApiKey("1ebd5527-a66e-4395-89f6-7ddb49c90439");
        MapKitFactory.initialize(this);
    }
}
