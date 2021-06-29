package com.bazaar.location.sample.service

import android.content.Intent
import android.location.Location
import android.os.IBinder
import bazaar.tech.library.location.base.LocationBaseService
import bazaar.tech.library.location.configuration.Configurations.silentConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType

class SampleService : LocationBaseService() {
    private var isLocationRequested = false
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override val locationConfiguration: LocationConfiguration
        get() = silentConfiguration(false)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // calling super is required when extending from LocationBaseService
        super.onStartCommand(intent, flags, startId)
        if (!isLocationRequested) {
            isLocationRequested = true
            location
        }

        // Return type is depends on your requirements
        return START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location?) {
        val intent = Intent(ACTION_LOCATION_CHANGED)
        intent.putExtra(EXTRA_LOCATION, location)
        sendBroadcast(intent)
        stopSelf()
    }

    override fun onLocationFailed(@FailType type: Int) {
        val intent = Intent(ACTION_LOCATION_FAILED)
        intent.putExtra(EXTRA_FAIL_TYPE, type)
        sendBroadcast(intent)
        stopSelf()
    }

    override fun onProcessTypeChanged(@ProcessType processType: Int) {
        val intent = Intent(ACTION_PROCESS_CHANGED)
        intent.putExtra(EXTRA_PROCESS_TYPE, processType)
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_LOCATION_CHANGED = "com.bazaar.location.sample.service.LOCATION_CHANGED"
        const val ACTION_LOCATION_FAILED = "com.bazaar.location.sample.service.LOCATION_FAILED"
        const val ACTION_PROCESS_CHANGED = "com.bazaar.location.sample.service.PROCESS_CHANGED"
        const val EXTRA_LOCATION = "ExtraLocationField"
        const val EXTRA_FAIL_TYPE = "ExtraFailTypeField"
        const val EXTRA_PROCESS_TYPE = "ExtraProcessTypeField"
    }
}