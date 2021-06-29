package bazaar.tech.library.location.base

import android.app.Service
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import bazaar.tech.library.location.LocationManager
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.listener.LocationListener

abstract class LocationBaseService : Service(), LocationListener {
    lateinit var locationManager: LocationManager
        private set
    abstract val locationConfiguration: LocationConfiguration
    @CallSuper
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        locationManager = LocationManager.Builder(applicationContext)
                .configuration(locationConfiguration)
                .notify(this)
                .build()
        return super.onStartCommand(intent, flags, startId)
    }

    protected val location: Unit
        get() {
            if (this::locationManager.isInitialized) {
                locationManager.get()
            } else {
                throw IllegalStateException("locationManager is null. "
                        + "Make sure you call super.onStartCommand before attempting to getLocation")
            }
        }

    override fun onProcessTypeChanged(@ProcessType processType: Int) {
        // override if needed
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        // override if needed
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // override if needed
    }

    override fun onProviderEnabled(provider: String?) {
        // override if needed
    }

    override fun onProviderDisabled(provider: String?) {
        // override if needed
    }
}