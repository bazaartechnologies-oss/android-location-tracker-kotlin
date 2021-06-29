package bazaar.tech.library.location.listener

import android.location.Location
import android.os.Bundle
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType

interface LocationListener {
    /**
     * This method will be called whenever the process is changed.
     *
     * @param processType new on going process
     */
    fun onProcessTypeChanged(@ProcessType processType: Int)

    /**
     * This method will be invoked whenever new location update received
     */
    fun onLocationChanged(location: Location?)

    /**
     * When it is not possible to receive location, such as no active provider or no permission etc.
     * It will pass an integer value from [FailType]
     * which will help you to determine how did it fail to receive location
     */
    fun onLocationFailed(@FailType type: Int)

    /**
     * This method will be invoked when user grants for location permissions,
     * or when you ask for it but the application already had that granted.
     * You can determine if permission is just granted or
     * did the application already have it by checking boolean input of this method.
     */
    fun onPermissionGranted(alreadyHadPermission: Boolean)

    /**
     * This method will be invoked if only you use android.location.LocationManager
     * with GPS or Network Providers to receive location
     */
    fun onStatusChanged(provider: String?, status: Int, extras: Bundle?)

    /**
     * This method will be invoked if only you use android.location.LocationManager
     * with GPS or Network Providers to receive location
     */
    fun onProviderEnabled(provider: String?)

    /**
     * This method will be invoked if only you use android.location.LocationManager
     * with GPS or Network Providers to receive location
     */
    fun onProviderDisabled(provider: String?)
}