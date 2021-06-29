package bazaar.tech.library.location.configuration

import android.Manifest
import com.google.android.gms.location.LocationRequest

object Defaults {
    private const val SECOND = 1000
    private const val MINUTE = 60 * SECOND
    const val WAIT_PERIOD = 20 * SECOND
    const val TIME_PERIOD = 5 * MINUTE
    const val LOCATION_DISTANCE_INTERVAL = 0
    const val LOCATION_INTERVAL = 5 * MINUTE
    const val MIN_ACCURACY = 5.0f
    const val KEEP_TRACKING = false
    const val FALLBACK_TO_DEFAULT = true
    const val ASK_FOR_GP_SERVICES = false
    const val ASK_FOR_SETTINGS_API = true
    const val FAIL_ON_SETTINGS_API_SUSPENDED = false
    const val IGNORE_LAST_KNOW_LOCATION = false
    const val EMPTY_STRING = ""
    const val GPS_MESSAGE = "Please turn on your GPS"
    const val PERMISSION_MESSAGE = "Please provide GPS permissions"

    @kotlin.jvm.JvmField
    val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
    private const val LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    private const val LOCATION_FASTEST_INTERVAL = MINUTE

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
     */
    fun createDefaultLocationRequest(): LocationRequest {
        return LocationRequest.create()
                .setPriority(LOCATION_PRIORITY)
                .setInterval(LOCATION_INTERVAL.toLong())
                .setFastestInterval(LOCATION_FASTEST_INTERVAL.toLong())
    }
}