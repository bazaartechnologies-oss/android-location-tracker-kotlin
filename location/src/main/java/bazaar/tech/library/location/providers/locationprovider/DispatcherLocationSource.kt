package bazaar.tech.library.location.providers.locationprovider

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.google.android.gms.common.GoogleApiAvailability
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask.ContinuousTaskRunner
import bazaar.tech.library.location.listener.FallbackListener

class DispatcherLocationSource(continuousTaskRunner: ContinuousTaskRunner) {
    private val gpServicesSwitchTask: ContinuousTask = ContinuousTask(GOOGLE_PLAY_SERVICE_SWITCH_TASK, continuousTaskRunner)

    fun createDefaultLocationProvider(): DefaultLocationProvider {
        return DefaultLocationProvider()
    }

    fun createGooglePlayServicesLocationProvider(fallbackListener: FallbackListener?): GooglePlayServicesLocationProvider {
        return GooglePlayServicesLocationProvider(fallbackListener)
    }

    fun gpServicesSwitchTask(): ContinuousTask {
        return gpServicesSwitchTask
    }

    fun isGoogleApiAvailable(context: Context?): Int {
        return if (context == null) -1 else GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    }

    fun isGoogleApiErrorUserResolvable(gpServicesAvailability: Int): Boolean {
        return GoogleApiAvailability.getInstance().isUserResolvableError(gpServicesAvailability)
    }

    fun getGoogleApiErrorDialog(activity: Activity?, gpServicesAvailability: Int, requestCode: Int,
                                onCancelListener: DialogInterface.OnCancelListener?): Dialog? {
        return if (activity == null) null else GoogleApiAvailability.getInstance()
                .getErrorDialog(activity, gpServicesAvailability, requestCode, onCancelListener)
    }

    companion object {
        const val GOOGLE_PLAY_SERVICE_SWITCH_TASK = "googlePlayServiceSwitchTask"
    }

}