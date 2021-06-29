package bazaar.tech.library.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import bazaar.tech.library.location.configuration.Configurations
import bazaar.tech.library.location.configuration.Defaults
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.helper.LogUtils
import bazaar.tech.library.location.helper.logging.DefaultLogger
import bazaar.tech.library.location.helper.logging.Logger
import bazaar.tech.library.location.listener.LocationListener
import bazaar.tech.library.location.listener.PermissionListener
import bazaar.tech.library.location.providers.locationprovider.*
import bazaar.tech.library.location.providers.permissionprovider.PermissionProvider
import bazaar.tech.library.location.view.ContextProcessor

class LocationManager private constructor(builder: Builder) : PermissionListener {
    private val listener: LocationListener?

    /**
     * Returns configuration object which is defined to this manager
     */
    var configuration: LocationConfiguration
    private var activeProvider: LocationProvider
    private var permissionProvider: PermissionProvider

    class Builder {
        var contextProcessor: ContextProcessor
        var listener: LocationListener? = null
        var configuration: LocationConfiguration = Configurations.defaultConfiguration(Defaults.PERMISSION_MESSAGE, Defaults.GPS_MESSAGE)
        lateinit var activeProvider: LocationProvider

        /**
         * Builder object to create LocationManager
         *
         * @param contextProcessor holds the address of the context,which this manager will run on
         */
        constructor(contextProcessor: ContextProcessor) {
            this.contextProcessor = contextProcessor
        }

        /**
         * Builder object to create LocationManager
         *
         * @param context MUST be an application context
         */
        constructor(context: Context) {
            contextProcessor = ContextProcessor(context)
        }

        /**
         * Activity is required in order to ask for permission, GPS enable dialog, Rationale dialog,
         * GoogleApiClient and SettingsApi.
         *
         * @param activity will be kept as weakReference
         */
        fun activity(activity: Activity): Builder {
            contextProcessor.setActivity(activity)
            return this
        }

        /**
         * Fragment is required in order to ask for permission, GPS enable dialog, Rationale dialog,
         * GoogleApiClient and SettingsApi.
         *
         * @param fragment will be kept as weakReference
         */
        fun fragment(fragment: Fragment): Builder {
            contextProcessor.setFragment(fragment)
            return this
        }

        /**
         * Configuration object in order to take decisions accordingly while trying to retrieve location
         */
        fun configuration(locationConfiguration: LocationConfiguration): Builder {
            configuration = locationConfiguration
            return this
        }

        /**
         * Instead of using [DispatcherLocationProvider] you can create your own,
         * and set it to manager so it will use given one.
         */
        fun locationProvider(provider: LocationProvider): Builder {
            activeProvider = provider
            return this
        }

        /**
         * Specify a LocationListener to receive location when it is available,
         * or get knowledge of any other steps in process
         */
        fun notify(listener: LocationListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): LocationManager {
            checkNotNull(configuration) { "You must set a configuration object." }
            if (!this::activeProvider.isInitialized) {
                locationProvider(DispatcherLocationProvider())
            }
            activeProvider.configure(contextProcessor, configuration, listener)
            return LocationManager(this)
        }
    }

    /**
     * Google suggests to stop location updates when the activity is no longer in focus
     * http://developer.android.com/training/location/receive-location-updates.html#stop-updates
     */
    fun onPause() {
        activeProvider.onPause()
    }

    /**
     * Restart location updates to keep continue getting locations when activity is back
     */
    fun onResume() {
        activeProvider.onResume()
    }

    /**
     * Release whatever you need to when onDestroy is called
     */
    fun onDestroy() {
        activeProvider.onDestroy()
    }

    /**
     * This is required to check when user handles with Google Play Services error, or enables GPS...
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activeProvider.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Provide requestPermissionResult to manager so the it can handle RuntimePermission
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionProvider.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * To determine whether LocationManager is currently waiting for location or it did already receive one!
     */
    val isWaitingForLocation: Boolean
        get() = activeProvider.isWaiting

    /**
     * To determine whether the manager is currently displaying any dialog or not
     */
    val isAnyDialogShowing: Boolean
        get() = activeProvider.isDialogShowing

    /**
     * Abort the mission and cancel all location update requests
     */
    fun cancel() {
        activeProvider.cancel()
    }

    /**
     * The only method you need to call to trigger getting location process
     */
    fun get() {
        askForPermission()
    }

    /**
     * Only For Test Purposes
     */
    fun activeProvider(): LocationProvider {
        return activeProvider
    }

    fun askForPermission() {
        if (permissionProvider.hasPermission()) {
            permissionGranted(true)
        } else {
            listener?.onProcessTypeChanged(ProcessType.ASKING_PERMISSIONS)
            if (permissionProvider.requestPermissions()) {
                LogUtils.logI("Waiting until we receive any callback from PermissionProvider...")
            } else {
                LogUtils.logI("Couldn't get permission, Abort!")
                failed(FailType.Companion.PERMISSION_DENIED)
            }
        }
    }

    private fun permissionGranted(alreadyHadPermission: Boolean) {
        LogUtils.logI("We got permission!")
        listener?.onPermissionGranted(alreadyHadPermission)
        activeProvider.get()
    }

    private fun failed(@FailType type: Int) {
        listener?.onLocationFailed(type)
    }

    override fun onPermissionsGranted() {
        permissionGranted(false)
    }

    override fun onPermissionsDenied() {
        failed(FailType.Companion.PERMISSION_DENIED)
    }

    companion object {
        /**
         * Library tries to log as much as possible in order to make it transparent to see what is actually going on
         * under the hood. You can enable it for debug purposes, but do not forget to disable on production.
         *
         * Log is disabled as default.
         */
        @kotlin.jvm.JvmStatic
        fun enableLog(enable: Boolean) {
            LogUtils.enable(enable)
        }

        /**
         * The Logger specifies how this Library is logging debug information. By default [DefaultLogger]
         * is used and it can be replaced by your own custom implementation of [Logger].
         */
        fun setLogger(logger: Logger) {
            LogUtils.setLogger(logger)
        }
    }

    /**
     * To create an instance of this manager you MUST specify a LocationConfiguration
     */
    init {
        listener = builder.listener
        configuration = builder.configuration
        activeProvider = builder.activeProvider
        permissionProvider = configuration.permissionConfiguration().permissionProvider
        permissionProvider.setContextProcessor(builder.contextProcessor)
        permissionProvider.setPermissionListener(this)
    }
}