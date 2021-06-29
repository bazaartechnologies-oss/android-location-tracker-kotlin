package bazaar.tech.library.location.providers.locationprovider

import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.helper.LogUtils
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import bazaar.tech.library.location.listener.DialogListener

class DefaultLocationProvider : LocationProvider(), ContinuousTaskRunner, LocationListener, DialogListener {
    private var defaultLocationSource: DefaultLocationSource? = null
    private var provider: String = LocationManager.GPS_PROVIDER
    private var gpsDialog: Dialog? = null

    override fun onDestroy() {
        super.onDestroy()
        gpsDialog = null
        sourceProvider.removeSwitchTask()
        sourceProvider.removeUpdateRequest()
        sourceProvider.removeLocationUpdates(this)
    }

    override fun cancel() {
        sourceProvider.updateRequest?.release()
        sourceProvider.providerSwitchTask?.stop()
    }

    override fun onPause() {
        super.onPause()
        sourceProvider.updateRequest?.release()
        sourceProvider.providerSwitchTask?.pause()
    }

    override fun onResume() {
        super.onResume()
        sourceProvider.updateRequest?.run()
        if (isWaiting) {
            sourceProvider.providerSwitchTask?.resume()
        }
        if (isDialogShowing && isGPSProviderEnabled) {
            // User activated GPS by going settings manually
            gpsDialog?.dismiss()
            onGPSActivated()
        }
    }

    override val isDialogShowing: Boolean
        get() = gpsDialog != null && gpsDialog!!.isShowing

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.GPS_ENABLE) {
            if (isGPSProviderEnabled) {
                onGPSActivated()
            } else {
                LogUtils.logI("User didn't activate GPS, so continue with Network Provider")
                locationByNetwork
            }
        }
    }

    override fun get() {
        isWaiting = true

        // First check for GPS
        if (isGPSProviderEnabled) {
            LogUtils.logI("GPS is already enabled, getting location...")
            askForLocation(LocationManager.GPS_PROVIDER)
        } else {
            // GPS is not enabled,
            if (configuration.defaultProviderConfiguration()?.askForEnableGPS() == true && activity != null) {
                LogUtils.logI("GPS is not enabled, asking user to enable it...")
                askForEnableGPS()
            } else {
                LogUtils.logI("GPS is not enabled, moving on with Network...")
                locationByNetwork
            }
        }
    }

    fun askForEnableGPS() {
        val gpsDialogProvider = configuration.defaultProviderConfiguration()?.gpsDialogProvider()
        gpsDialogProvider?.dialogListener = this
        gpsDialog = gpsDialogProvider?.getDialog(activity!!)
        gpsDialog?.show()
    }

    fun onGPSActivated() {
        LogUtils.logI("User activated GPS, listen for location")
        askForLocation(LocationManager.GPS_PROVIDER)
    }

    val locationByNetwork: Unit
        get() {
            if (isNetworkProviderEnabled) {
                LogUtils.logI("Network is enabled, getting location...")
                askForLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                LogUtils.logI("Network is not enabled, calling fail...")
                onLocationFailed(FailType.NETWORK_NOT_AVAILABLE)
            }
        }

    fun askForLocation(provider: String) {
        sourceProvider.providerSwitchTask?.stop()
        setCurrentProvider(provider)
        val locationIsAlreadyAvailable = checkForLastKnowLocation()
        if (configuration.keepTracking() || !locationIsAlreadyAvailable) {
            LogUtils.logI("Ask for location update...")
            notifyProcessChange()
            if (!locationIsAlreadyAvailable) {
                sourceProvider.providerSwitchTask?.delayed(waitPeriod)
            }
            requestUpdateLocation()
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.")
        }
    }

    fun checkForLastKnowLocation(): Boolean {
        val lastKnownLocation = sourceProvider.getLastKnownLocation(provider)
        if (sourceProvider.isLocationSufficient(lastKnownLocation,
                        configuration.defaultProviderConfiguration().acceptableTimePeriod(),
                        configuration.defaultProviderConfiguration().acceptableAccuracy())) {
            LogUtils.logI("LastKnowLocation is usable.")
            onLocationReceived(lastKnownLocation)
            return true
        } else {
            LogUtils.logI("LastKnowLocation is not usable.")
        }
        return false
    }

    fun setCurrentProvider(provider: String) {
        this.provider = provider
    }

    fun notifyProcessChange() {
        listener?.onProcessTypeChanged(if (LocationManager.GPS_PROVIDER == provider) ProcessType.Companion.GETTING_LOCATION_FROM_GPS_PROVIDER else ProcessType.Companion.GETTING_LOCATION_FROM_NETWORK_PROVIDER)
    }

    fun requestUpdateLocation() {
        val timeInterval = configuration.defaultProviderConfiguration().requiredTimeInterval()
        val distanceInterval = configuration.defaultProviderConfiguration().requiredDistanceInterval()
        sourceProvider.updateRequest?.run(provider, timeInterval, distanceInterval.toFloat())
    }

    val waitPeriod: Long
        get() = if (LocationManager.GPS_PROVIDER == provider) configuration.defaultProviderConfiguration().gpsWaitPeriod() else configuration.defaultProviderConfiguration().networkWaitPeriod()
    private val isNetworkProviderEnabled: Boolean
        get() = sourceProvider.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    private val isGPSProviderEnabled: Boolean
        get() = sourceProvider.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun onLocationReceived(location: Location?) {
        listener?.onLocationChanged(location)
        isWaiting = false
    }

    fun onLocationFailed(@FailType type: Int) {
        listener?.onLocationFailed(type)
        isWaiting = false
    }

    override fun onLocationChanged(location: Location) {
        if (sourceProvider.updateRequestIsRemoved()) {
            return
        }
        onLocationReceived(location)

        // Remove cancelLocationTask because we have already find location,
        // no need to switch or call fail
        if (!sourceProvider.switchTaskIsRemoved()) {
            sourceProvider.providerSwitchTask?.stop()
        }
        if (!configuration.keepTracking()) {
            sourceProvider.updateRequest?.release()
            sourceProvider.removeLocationUpdates(this)
        }
    }

    /**
     * This callback will never be invoked on Android Q and above, and providers can be considered as always in the LocationProvider#AVAILABLE state.
     *
     * @see [](https://developer.android.com/reference/android/location/LocationListener.onStatusChanged
    ) */
    @Deprecated("")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
        listener?.onStatusChanged(provider, status, extras)
    }

    override fun onProviderEnabled(provider: String) {
        listener?.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        listener?.onProviderDisabled(provider)
    }

    override fun runScheduledTask(taskId: String) {
        if (taskId == DefaultLocationSource.PROVIDER_SWITCH_TASK) {
            sourceProvider.updateRequest?.release()
            if (LocationManager.GPS_PROVIDER == provider) {
                LogUtils.logI("We waited enough for GPS, switching to Network provider...")
                locationByNetwork
            } else {
                LogUtils.logI("Network Provider is not provide location in required period, calling fail...")
                onLocationFailed(FailType.Companion.TIMEOUT)
            }
        }
    }

    override fun onPositiveButtonClick() {
        val activityStarted = startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                RequestCode.GPS_ENABLE)
        if (!activityStarted) {
            onLocationFailed(FailType.Companion.VIEW_NOT_REQUIRED_TYPE)
        }
    }

    override fun onNegativeButtonClick() {
        LogUtils.logI("User didn't want to enable GPS, so continue with Network Provider")
        locationByNetwork
    }

    // For test purposes
    fun setDefaultLocationSource(defaultLocationSource: DefaultLocationSource?) {
        this.defaultLocationSource = defaultLocationSource
    }

    private val sourceProvider: DefaultLocationSource
        get() {
            if (defaultLocationSource == null) {
                defaultLocationSource = DefaultLocationSource(context, this, this)
            }
            return defaultLocationSource!!
        }
}