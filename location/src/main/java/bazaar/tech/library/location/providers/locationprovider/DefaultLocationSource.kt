package bazaar.tech.library.location.providers.locationprovider

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import bazaar.tech.library.location.helper.UpdateRequest
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import java.util.*

class DefaultLocationSource(
        context: Context,
        continuousTaskRunner: ContinuousTaskRunner,
        locationListener: LocationListener) {
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var updateRequest: UpdateRequest?
        private set
    var providerSwitchTask: ContinuousTask?
        private set

    fun isProviderEnabled(provider: String): Boolean {
        return locationManager.isProviderEnabled(provider)
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(provider: String): Location? {
        return locationManager.getLastKnownLocation(provider)
    }

    fun removeLocationUpdates(locationListener: LocationListener) {
        locationManager.removeUpdates(locationListener)
    }

    fun removeUpdateRequest() {
        updateRequest?.release()
        updateRequest = null
    }

    fun removeSwitchTask() {
        providerSwitchTask?.stop()
        providerSwitchTask = null
    }

    fun switchTaskIsRemoved(): Boolean {
        return providerSwitchTask == null
    }

    fun updateRequestIsRemoved(): Boolean {
        return updateRequest == null
    }

    fun isLocationSufficient(location: Location?, acceptableTimePeriod: Long, acceptableAccuracy: Float): Boolean {
        if (location == null) return false
        val givenAccuracy = location.accuracy
        val givenTime = location.time
        val minAcceptableTime = Date().time - acceptableTimePeriod
        return minAcceptableTime <= givenTime && acceptableAccuracy >= givenAccuracy
    }

    companion object {
        const val PROVIDER_SWITCH_TASK = "providerSwitchTask"
    }

    init {
        updateRequest = UpdateRequest(locationManager, locationListener)
        providerSwitchTask = ContinuousTask(PROVIDER_SWITCH_TASK, continuousTaskRunner)
    }
}