package bazaar.tech.library.location.helper

import android.location.LocationListener
import android.location.LocationManager

class UpdateRequest(private val locationManager: LocationManager?, private val locationListener: LocationListener) {
    private var provider: String? = null
    private var minTime: Long = 0
    private var minDistance = 0f
    fun run(provider: String?, minTime: Long, minDistance: Float) {
        this.provider = provider
        this.minTime = minTime
        this.minDistance = minDistance
        run()
    }

    fun run() {
        if (StringUtils.isNotEmpty(provider)) {
            locationManager!!.requestLocationUpdates(provider, minTime, minDistance, locationListener)
        }
    }

    fun release() {
        locationManager?.removeUpdates(locationListener)
    }
}