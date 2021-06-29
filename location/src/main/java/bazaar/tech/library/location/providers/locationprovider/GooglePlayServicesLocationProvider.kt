package bazaar.tech.library.location.providers.locationprovider

import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Location
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.helper.LogUtils
import bazaar.tech.library.location.listener.FallbackListener
import bazaar.tech.library.location.providers.locationprovider.GooglePlayServicesLocationSource.SourceListener
import java.lang.ref.WeakReference

class GooglePlayServicesLocationProvider internal constructor(fallbackListener: FallbackListener?) : LocationProvider(), SourceListener {
    private val fallbackListener: WeakReference<FallbackListener?> = WeakReference(fallbackListener)

    override var isDialogShowing = false
        private set
    private var googlePlayServicesLocationSource: GooglePlayServicesLocationSource? = null
    override fun onResume() {
        if (!isDialogShowing && (isWaiting || configuration.keepTracking())) {
            requestLocationUpdate()
        }
    }

    override fun onPause() {
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (!isDialogShowing && googlePlayServicesLocationSource != null) {
            removeLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) removeLocationUpdates()
    }

    override fun get() {
        isWaiting = true
        if (context != null) {
            LogUtils.logI("Start request location updates.")
            if (configuration.googlePlayServicesConfiguration().ignoreLastKnowLocation()) {
                LogUtils.logI("Configuration requires to ignore last know location from GooglePlayServices Api.")

                // Request fresh location
                locationRequired()
            } else {
                // Try to get last location, if failed then request fresh location
                sourceProvider.requestLastLocation()
            }
        } else {
            failed(FailType.Companion.VIEW_DETACHED)
        }
    }

    override fun cancel() {
        LogUtils.logI("Canceling GooglePlayServiceLocationProvider...")
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) {
            removeLocationUpdates()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.SETTINGS_API) {
            isDialogShowing = false
            if (resultCode == Activity.RESULT_OK) {
                LogUtils.logI("We got settings changed, requesting location update...")
                requestLocationUpdate()
            } else {
                LogUtils.logI("User denied settingsApi dialog, GooglePlayServices SettingsApi failing...")
                settingsApiFail(FailType.Companion.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
            }
        }
    }

    fun onLocationChanged(location: Location) {
        listener?.onLocationChanged(location)

        // Set waiting as false because we got at least one, even though we keep tracking user's location
        isWaiting = false
        if (!configuration.keepTracking()) {
            // If need to update location once, clear the listener to prevent multiple call
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.")
            removeLocationUpdates()
        }
    }

    override fun onLocationResult(locationResult: LocationResult) {
        for (location in locationResult.locations) {
            onLocationChanged(location)
        }
    }

    override fun onSuccess(locationSettingsResponse: LocationSettingsResponse) {
        // All location settings are satisfied. The client can initialize location
        // requests here.
        LogUtils.logI("We got GPS, Wifi and/or Cell network providers enabled enough "
                + "to receive location as we needed. Requesting location update...")
        requestLocationUpdate()
    }

    override fun onFailure(exception: Exception) {
        val statusCode = (exception as ApiException).statusCode
        when (statusCode) {
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                // Location settings are not satisfied.
                // However, we have no way to fix the settings so we won't show the dialog.
                LogUtils.logE("Settings change is not available, SettingsApi failing...")
                settingsApiFail(FailType.Companion.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                 // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                // Cast to a resolvable exception.
                resolveSettingsApi(exception as ResolvableApiException)
            else -> {
                // for other CommonStatusCodes values
                LogUtils.logE("LocationSettings failing, status: " + CommonStatusCodes.getStatusCodeString(statusCode))
                settingsApiFail(FailType.Companion.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
            }
        }
    }

    fun resolveSettingsApi(resolvable: ResolvableApiException) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            LogUtils.logI("We need settingsApi dialog to switch required settings on.")
            if (activity != null) {
                LogUtils.logI("Displaying the dialog...")
                sourceProvider.startSettingsApiResolutionForResult(resolvable, activity!!)
                isDialogShowing = true
            } else {
                LogUtils.logI("Settings Api cannot show dialog if LocationManager is not running on an activity!")
                settingsApiFail(FailType.Companion.VIEW_NOT_REQUIRED_TYPE)
            }
        } catch (e: SendIntentException) {
            LogUtils.logE("Error on displaying SettingsApi dialog, SettingsApi failing...")
            settingsApiFail(FailType.Companion.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
        }
    }

    /**
     * Task result can be null in certain conditions
     * See: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient#getLastLocation()
     */
    override fun onLastKnowLocationTaskReceived(task: Task<Location>) {
        if (task.isSuccessful && task.result != null) {
            val lastKnownLocation = task.result
            LogUtils.logI("LastKnowLocation is available.")
            onLocationChanged(lastKnownLocation)
            if (configuration.keepTracking()) {
                LogUtils.logI("Configuration requires keepTracking.")
                locationRequired()
            }
        } else {
            LogUtils.logI("LastKnowLocation is not available.")
            locationRequired()
        }
    }

    fun locationRequired() {
        LogUtils.logI("Ask for location update...")
        if (configuration.googlePlayServicesConfiguration().askForSettingsApi()) {
            LogUtils.logI("Asking for SettingsApi...")
            sourceProvider.checkLocationSettings()
        } else {
            LogUtils.logI("SettingsApi is not enabled, requesting for location update...")
            requestLocationUpdate()
        }
    }

    fun requestLocationUpdate() {
        listener?.onProcessTypeChanged(ProcessType.Companion.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES)
        LogUtils.logI("Requesting location update...")
        sourceProvider.requestLocationUpdate()
    }

    fun settingsApiFail(@FailType failType: Int) {
        if (configuration.googlePlayServicesConfiguration().failOnSettingsApiSuspended()) {
            failed(failType)
        } else {
            LogUtils.logE("Even though settingsApi failed, configuration requires moving on. "
                    + "So requesting location update...")
            requestLocationUpdate()
        }
    }

    fun failed(@FailType type: Int) {
        if (configuration.googlePlayServicesConfiguration().fallbackToDefault() && fallbackListener.get() != null) {
            fallbackListener.get()?.onFallback()
        } else {
            listener?.onLocationFailed(type)
        }
        isWaiting = false
    }

    // For test purposes
    fun setDispatcherLocationSource(googlePlayServicesLocationSource: GooglePlayServicesLocationSource?) {
        this.googlePlayServicesLocationSource = googlePlayServicesLocationSource
    }

    private val sourceProvider: GooglePlayServicesLocationSource
        get() {
            if (googlePlayServicesLocationSource == null) {
                googlePlayServicesLocationSource = GooglePlayServicesLocationSource(context,
                        configuration.googlePlayServicesConfiguration().locationRequest(), this)
            }
            return googlePlayServicesLocationSource!!
        }

    private fun removeLocationUpdates() {
        LogUtils.logI("Stop location updates...")

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (googlePlayServicesLocationSource != null) {
            isWaiting = false
            googlePlayServicesLocationSource!!.removeLocationUpdates()
        }
    }

}