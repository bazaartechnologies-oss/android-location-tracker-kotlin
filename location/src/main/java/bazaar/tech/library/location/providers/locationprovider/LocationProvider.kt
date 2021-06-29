package bazaar.tech.library.location.providers.locationprovider

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.helper.LogUtils
import bazaar.tech.library.location.listener.LocationListener
import bazaar.tech.library.location.view.ContextProcessor
import java.lang.ref.WeakReference

abstract class LocationProvider {
    /**
     * Returns waiting state
     */
    /**
     * Call this method while you begin to process getting location
     * and call it when at least one location is received
     */
    open var isWaiting = false
    lateinit var configuration: LocationConfiguration
        private set
    lateinit var contextProcessor: ContextProcessor
    private var weakLocationListener: WeakReference<LocationListener?>? = null

    /**
     * This method is called immediately once the LocationProvider is set to [LocationManager]
     */
    @CallSuper
    fun configure(contextProcessor: ContextProcessor, configuration: LocationConfiguration,
                  listener: LocationListener?) {
        this.contextProcessor = contextProcessor
        this.configuration = configuration
        weakLocationListener = WeakReference(listener)
        initialize()
    }

    /**
     * This is used for passing object between LocationProviders
     */
    @CallSuper
    fun configure(locationProvider: LocationProvider) {
        contextProcessor = locationProvider.contextProcessor
        configuration = locationProvider.configuration
        weakLocationListener = locationProvider.weakLocationListener
        initialize()
    }

    /**
     * This method will be used to determine whether any LocationProvider
     * is currently displaying dialog or something.
     */
    abstract val isDialogShowing: Boolean

    /**
     * This is where your provider actually starts working
     */
    abstract fun get()

    /**
     * This provider is asked to be canceled all tasks currently running
     * and remove all location update listeners
     */
    abstract fun cancel()

    /**
     * Override when you need to handle activityResult such as listening for GPS activation
     */
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    /**
     * This is called right after configurations are set
     */
    fun initialize() {}

    /**
     * To remove location updates while getting from GPS or Network Provider
     */
    @CallSuper
    open fun onDestroy() {
        weakLocationListener?.clear()
    }

    open fun onPause() {}
    open fun onResume() {}

    protected val listener: LocationListener?
        get() = weakLocationListener?.get()
    protected val context: Context
        get() = contextProcessor.context
    protected val activity: Activity?
        get() = contextProcessor.activity
    protected val fragment: Fragment?
        get() = contextProcessor.fragment

    protected fun startActivityForResult(intent: Intent?, requestCode: Int): Boolean {
        when {
            fragment != null -> {
                fragment?.startActivityForResult(intent, requestCode)
            }
            activity != null -> {
                activity?.startActivityForResult(intent, requestCode)
            }
            else -> {
                LogUtils.logE("Cannot startActivityForResult because host is neither Activity nor Fragment.")
                return false
            }
        }
        return true
    }
}