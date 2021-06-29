package bazaar.tech.library.location.base

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import bazaar.tech.library.location.LocationManager
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.listener.LocationListener

abstract class LocationBaseFragment : Fragment(), LocationListener {
    lateinit var locationManager: LocationManager
        private set
    abstract val locationConfiguration: LocationConfiguration
    protected val location: Unit
        get() {
            if (this::locationManager.isInitialized) {
                locationManager.get()
            } else {
                throw IllegalStateException("locationManager is null. "
                        + "Make sure you call super.initialize before attempting to getLocation")
            }
        }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager.Builder(requireContext().applicationContext)
                .configuration(locationConfiguration)
                .fragment(this)
                .notify(this)
                .build()
    }

    @CallSuper
    override fun onDestroy() {
        locationManager.onDestroy()
        super.onDestroy()
    }

    @CallSuper
    override fun onPause() {
        locationManager.onPause()
        super.onPause()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        locationManager.onResume()
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationManager.onActivityResult(requestCode, resultCode, data)
    }

    @CallSuper
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
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