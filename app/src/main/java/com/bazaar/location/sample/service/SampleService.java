package com.bazaar.location.sample.service;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.Nullable;

import bazaar.tech.library.location.base.LocationBaseService;
import bazaar.tech.library.location.configuration.Configurations;
import bazaar.tech.library.location.configuration.LocationConfiguration;
import bazaar.tech.library.location.constants.FailType;
import bazaar.tech.library.location.constants.ProcessType;

public class SampleService extends LocationBaseService {

    public static final String ACTION_LOCATION_CHANGED = "com.bazaar.location.sample.service.LOCATION_CHANGED";
    public static final String ACTION_LOCATION_FAILED = "com.bazaar.location.sample.service.LOCATION_FAILED";
    public static final String ACTION_PROCESS_CHANGED = "com.bazaar.location.sample.service.PROCESS_CHANGED";

    public static final String EXTRA_LOCATION = "ExtraLocationField";
    public static final String EXTRA_FAIL_TYPE = "ExtraFailTypeField";
    public static final String EXTRA_PROCESS_TYPE = "ExtraProcessTypeField";

    private boolean isLocationRequested = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.silentConfiguration(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // calling super is required when extending from LocationBaseService
        super.onStartCommand(intent, flags, startId);

        if(!isLocationRequested) {
            isLocationRequested = true;
            getLocation();
        }

        // Return type is depends on your requirements
        return START_NOT_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_CHANGED);
        intent.putExtra(EXTRA_LOCATION, location);
        sendBroadcast(intent);

        stopSelf();
    }

    @Override
    public void onLocationFailed(@FailType int type) {
        Intent intent = new Intent(ACTION_LOCATION_FAILED);
        intent.putExtra(EXTRA_FAIL_TYPE, type);
        sendBroadcast(intent);

        stopSelf();
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        Intent intent = new Intent(ACTION_PROCESS_CHANGED);
        intent.putExtra(EXTRA_PROCESS_TYPE, processType);
        sendBroadcast(intent);
    }
}
