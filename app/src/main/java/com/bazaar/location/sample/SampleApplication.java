package com.bazaar.location.sample;

import android.app.Application;

import bazaar.tech.library.location.LocationManager;


public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager.enableLog(true);
    }
}
