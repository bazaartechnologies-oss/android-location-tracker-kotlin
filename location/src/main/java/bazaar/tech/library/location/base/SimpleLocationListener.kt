package bazaar.tech.library.location.base

import android.os.Bundle
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.listener.LocationListener

/**
 * Empty Location Listener in case you need only some of the methods from [LocationListener]
 * Only [LocationListener.onLocationChanged] and [LocationListener.onLocationFailed]
 * need to be overridden.
 */
abstract class SimpleLocationListener : LocationListener {
    override fun onProcessTypeChanged(@ProcessType processType: Int) {}
    override fun onPermissionGranted(alreadyHadPermission: Boolean) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}
}