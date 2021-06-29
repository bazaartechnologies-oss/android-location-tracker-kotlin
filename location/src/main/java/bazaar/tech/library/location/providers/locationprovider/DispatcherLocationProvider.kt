package bazaar.tech.library.location.providers.locationprovider

import android.app.Dialog
import android.content.Intent
import com.google.android.gms.common.ConnectionResult
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.helper.LogUtils
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import bazaar.tech.library.location.listener.FallbackListener

class DispatcherLocationProvider : LocationProvider(), ContinuousTaskRunner, FallbackListener {
    private var gpServicesDialog: Dialog? = null
    private var activeProvider: LocationProvider = sourceProvider.createGooglePlayServicesLocationProvider(this)
    private var dispatcherLocationSource: DispatcherLocationSource? = null
    override fun onPause() {
        super.onPause()
        activeProvider.onPause()
        sourceProvider.gpServicesSwitchTask().pause()
    }

    override fun onResume() {
        super.onResume()
        activeProvider.onResume()
        sourceProvider.gpServicesSwitchTask().resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeProvider.onDestroy()
        sourceProvider.gpServicesSwitchTask().stop()
        dispatcherLocationSource = null
        gpServicesDialog = null
    }

    override fun cancel() {
        activeProvider.cancel()
        sourceProvider.gpServicesSwitchTask().stop()
    }

    override var isWaiting: Boolean
        get() = activeProvider.isWaiting
        set(isWaiting) {
            super.isWaiting = isWaiting
        }
    override val isDialogShowing: Boolean
        get() {
            val gpServicesDialogShown = gpServicesDialog != null && gpServicesDialog!!.isShowing
            val anyProviderDialogShown = activeProvider.isDialogShowing
            return gpServicesDialogShown || anyProviderDialogShown
        }

    override fun runScheduledTask(taskId: String) {
        if (taskId == DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK) {
            if (activeProvider is GooglePlayServicesLocationProvider && activeProvider.isWaiting) {
                LogUtils.logI("We couldn't receive location from GooglePlayServices, so switching default providers...")
                cancel()
                continueWithDefaultProviders()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.GOOGLE_PLAY_SERVICES) {
            // Check whether do we have gpServices now or still not!
            checkGooglePlayServicesAvailability(false)
        } else {
            activeProvider!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun get() {
        if (configuration.googlePlayServicesConfiguration() != null) {
            checkGooglePlayServicesAvailability(true)
        } else {
            LogUtils.logI("Configuration requires not to use Google Play Services, "
                    + "so skipping that step to Default Location Providers")
            continueWithDefaultProviders()
        }
    }

    override fun onFallback() {
        // This is called from GooglePlayServicesLocationProvider when it fails to before its scheduled time
        cancel()
        continueWithDefaultProviders()
    }

    fun checkGooglePlayServicesAvailability(askForGooglePlayServices: Boolean) {
        val gpServicesAvailability = sourceProvider.isGoogleApiAvailable(context)
        if (gpServicesAvailability == ConnectionResult.SUCCESS) {
            LogUtils.logI("GooglePlayServices is available on device.")
            locationFromGooglePlayServices
        } else {
            LogUtils.logI("GooglePlayServices is NOT available on device.")
            if (askForGooglePlayServices) {
                askForGooglePlayServices(gpServicesAvailability)
            } else {
                LogUtils.logI("GooglePlayServices is NOT available and even though we ask user to handle error, "
                        + "it is still NOT available.")

                // This means get method is called by onActivityResult
                // which we already ask user to handle with gpServices error
                continueWithDefaultProviders()
            }
        }
    }

    fun askForGooglePlayServices(gpServicesAvailability: Int) {
        if (configuration.googlePlayServicesConfiguration().askForGooglePlayServices() &&
                sourceProvider.isGoogleApiErrorUserResolvable(gpServicesAvailability)) {
            resolveGooglePlayServices(gpServicesAvailability)
        } else {
            LogUtils.logI("Either GooglePlayServices error is not resolvable "
                    + "or the configuration doesn't wants us to bother user.")
            continueWithDefaultProviders()
        }
    }

    /**
     * Handle GooglePlayServices error. Try showing a dialog that maybe can fix the error by user action.
     * If error cannot be resolved or user cancelled dialog or dialog cannot be displayed, then [.continueWithDefaultProviders] is called.
     *
     *
     * The [com.google.android.gms.common.GoogleApiAvailability.isGooglePlayServicesAvailable] returns one of following in [ConnectionResult]:
     * SUCCESS, SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID.
     *
     *
     * See https://developers.google.com/android/reference/com/google/android/gms/common/GoogleApiAvailability#public-int-isgoogleplayservicesavailable-context-context
     */
    fun resolveGooglePlayServices(gpServicesAvailability: Int) {
        LogUtils.logI("Asking user to handle GooglePlayServices error...")
        gpServicesDialog = sourceProvider.getGoogleApiErrorDialog(activity, gpServicesAvailability,
                RequestCode.GOOGLE_PLAY_SERVICES) {
            LogUtils.logI("GooglePlayServices error could've been resolved, "
                    + "but user canceled it.")
            continueWithDefaultProviders()
        }
        if (gpServicesDialog != null) {

            /*
            The SERVICE_INVALID, SERVICE_UPDATING errors cannot be resolved via user action.
            In these cases, when user closes dialog by clicking OK button, OnCancelListener is not called.
            So, to handle these errors, we attach a dismiss event listener that calls continueWithDefaultProviders(), when dialog is closed.
             */
            when (gpServicesAvailability) {
                ConnectionResult.SERVICE_INVALID, ConnectionResult.SERVICE_UPDATING -> gpServicesDialog!!.setOnDismissListener {
                    LogUtils.logI("GooglePlayServices error could not have been resolved")
                    continueWithDefaultProviders()
                }
            }
            gpServicesDialog!!.show()
        } else {
            LogUtils.logI("GooglePlayServices error could've been resolved, but since LocationManager "
                    + "is not running on an Activity, dialog cannot be displayed.")
            continueWithDefaultProviders()
        }
    }

    val locationFromGooglePlayServices: Unit
        get() {
            LogUtils.logI("Attempting to get location from Google Play Services providers...")
            setLocationProvider(sourceProvider.createGooglePlayServicesLocationProvider(this))
            sourceProvider.gpServicesSwitchTask().delayed(configuration
                    .googlePlayServicesConfiguration().googlePlayServicesWaitPeriod())
            activeProvider.get()
        }

    /**
     * Called in case of Google Play Services failed to retrieve location,
     * or GooglePlayServicesConfiguration doesn't provided by developer
     */
    fun continueWithDefaultProviders() {
        LogUtils.logI("Attempting to get location from default providers...")
        setLocationProvider(sourceProvider.createDefaultLocationProvider())
        activeProvider.get()
    }

    fun setLocationProvider(provider: LocationProvider) {
        activeProvider = provider
        activeProvider.configure(this)
    }

    // For test purposes
    fun setDispatcherLocationSource(dispatcherLocationSource: DispatcherLocationSource?) {
        this.dispatcherLocationSource = dispatcherLocationSource
    }

    private val sourceProvider: DispatcherLocationSource
        get() {
            if (dispatcherLocationSource == null) {
                dispatcherLocationSource = DispatcherLocationSource(this)
            }
            return dispatcherLocationSource!!
        }
}