package bazaar.tech.library.location.locationmanager.providers.locationprovider

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.location.Location
import android.location.LocationManager
import bazaar.tech.library.location.configuration.DefaultProviderConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.helper.UpdateRequest
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask
import bazaar.tech.library.location.listener.LocationListener
import bazaar.tech.library.location.providers.dialogprovider.DialogProvider
import bazaar.tech.library.location.providers.locationprovider.DefaultLocationProvider
import bazaar.tech.library.location.providers.locationprovider.DefaultLocationSource
import bazaar.tech.library.location.view.ContextProcessor
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class DefaultLocationProviderTest {
    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var dialog: Dialog

    @Mock
    lateinit var continuousTask: ContinuousTask

    @Mock
    lateinit var updateRequest: UpdateRequest

    @Mock
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var defaultProviderConfiguration: DefaultProviderConfiguration

    @Mock
    lateinit var dialogProvider: DialogProvider

    @Mock
    lateinit var defaultLocationSource: DefaultLocationSource

    lateinit var defaultLocationProvider: DefaultLocationProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(locationConfiguration.defaultProviderConfiguration()).thenReturn(defaultProviderConfiguration)
        Mockito.`when`(defaultProviderConfiguration.gpsDialogProvider()).thenReturn(dialogProvider)
        Mockito.`when`(dialogProvider.getDialog(context)).thenReturn(dialog)
        Mockito.`when`(contextProcessor.context).thenReturn(context)
        Mockito.`when`(contextProcessor.activity).thenReturn(activity)
        Mockito.`when`(defaultLocationSource.providerSwitchTask).thenReturn(continuousTask)
        Mockito.`when`(defaultLocationSource.updateRequest).thenReturn(updateRequest)
        defaultLocationProvider = Mockito.spy(DefaultLocationProvider())
        defaultLocationProvider.setDefaultLocationSource(defaultLocationSource)
        defaultLocationProvider.configure(contextProcessor, locationConfiguration, locationListener)
    }

    @Test
    fun configureShouldInvokeInitialize() {
        val provider = Mockito.spy(DefaultLocationProvider())
        provider.configure(defaultLocationProvider)
        Mockito.verify(provider).initialize()
    }

    @Test
    fun onDestroyShouldRemoveInstances() {
        defaultLocationProvider!!.onDestroy()
        Mockito.verify(defaultLocationSource).removeLocationUpdates(defaultLocationProvider!!)
        Mockito.verify(defaultLocationSource).removeSwitchTask()
        Mockito.verify(defaultLocationSource).removeUpdateRequest()
    }

    @Test
    fun cancelShouldStopTasks() {
        defaultLocationProvider!!.cancel()
        Mockito.verify(updateRequest).release()
        Mockito.verify(continuousTask).stop()
    }

    @Test
    fun onPauseShouldPauseTasks() {
        defaultLocationProvider!!.onPause()
        Mockito.verify(updateRequest).release()
        Mockito.verify(continuousTask).pause()
    }

    @Test
    fun onResumeShouldResumeUpdateRequest() {
        defaultLocationProvider!!.onResume()
        Mockito.verify(updateRequest).run()
    }

    @Test
    fun onResumeShouldResumeSwitchTaskWhenLocationIsStillRequired() {
        defaultLocationProvider!!.isWaiting = true
        defaultLocationProvider!!.onResume()
        Mockito.verify(continuousTask).resume()
    }

//    @Test
//    fun onResumeDialogShouldDismissWhenDialogIsOnAndGPSIsActivated() {
//        defaultLocationProvider!!.askForEnableGPS() // to get dialog initialized
//        whenever(dialog.isShowing).thenReturn(true)
//        enableLocationProvider()
//        defaultLocationProvider!!.onResume()
//        Mockito.verify(dialog).dismiss()
//        Mockito.verify(defaultLocationProvider).onGPSActivated()
//    }

    @Test
    fun onResumeDialogShouldNotDismissedWhenGPSNotActivated() {
        defaultLocationProvider!!.askForEnableGPS() // to get dialog initialized
        Mockito.`when`(dialog!!.isShowing).thenReturn(true)
        disableLocationProvider()
        defaultLocationProvider!!.onResume()
        Mockito.verify(dialog, Mockito.never()).dismiss()
    }

    @Test
    fun isDialogShowingShouldReturnFalseWhenGPSDialogIsNotOn() {
            Assertions.assertThat(defaultLocationProvider!!.isDialogShowing).isFalse
        }

//    // to get dialog initialized
//    @Test
//    fun isDialogShowingShouldReturnTrueWhenGPSDialogIsOn() {
//            defaultLocationProvider!!.askForEnableGPS() // to get dialog initialized
//            Mockito.`when`(dialog!!.isShowing).thenReturn(true)
//            Assertions.assertThat(defaultLocationProvider!!.isDialogShowing).isTrue
//        }

    @Test
    fun onActivityResultShouldCallOnGPSActivated() {
        enableLocationProvider()
        defaultLocationProvider!!.onActivityResult(RequestCode.GPS_ENABLE, -1, null)
        Mockito.verify(defaultLocationProvider).onGPSActivated()
    }

    @Test
    fun onActivityResultShouldCallGetLocationByNetworkWhenGPSIsNotEnabled() {
        disableLocationProvider()
        defaultLocationProvider!!.onActivityResult(RequestCode.GPS_ENABLE, -1, null)
        Mockito.verify(defaultLocationProvider).locationByNetwork
    }

    @Test
    fun shouldSetWaitingTrue () {
            enableLocationProvider()
            Assertions.assertThat(defaultLocationProvider!!.isWaiting).isFalse
            defaultLocationProvider!!.get()
            Assertions.assertThat(defaultLocationProvider!!.isWaiting).isTrue
        }

    @Test
    fun shouldAskForLocationWithGPSProviderWhenItIsEnabled() {
            enableLocationProvider()
            defaultLocationProvider!!.get()
            Mockito.verify(defaultLocationProvider).askForLocation(GPS_PROVIDER)
        }

    @Test
    fun shouldAskForEnableGPSWhenGPSIsNotEnabledButRequiredByConfigurationToAskForIt() {
            disableLocationProvider()
            Mockito.`when`(defaultProviderConfiguration!!.askForEnableGPS()).thenReturn(true)
            defaultLocationProvider!!.get()
            Mockito.verify(defaultLocationProvider).askForEnableGPS()
        }

    @Test
fun shouldGetLocationByNetworkWhenGPSNotRequiredToAsk() {
            disableLocationProvider()
            Mockito.`when`(defaultProviderConfiguration!!.askForEnableGPS()).thenReturn(false)
            defaultLocationProvider!!.get()
            Mockito.verify(defaultLocationProvider).locationByNetwork
        }

    @Test
fun shouldGetLocationByNetworkWhenGPSNotEnabledAndThereIsNoActivity() {
            disableLocationProvider()
            Mockito.`when`(defaultProviderConfiguration!!.askForEnableGPS()).thenReturn(true)
            Mockito.`when`(contextProcessor!!.activity).thenReturn(null)
            defaultLocationProvider!!.get()
            Mockito.verify(defaultLocationProvider).locationByNetwork
        }

//    @Test
//    fun askForEnableGPSShouldShowDialog() {
//        defaultLocationProvider!!.askForEnableGPS()
//        Mockito.verify(dialogProvider).dialogListener = defaultLocationProvider
//        Mockito.verify(dialogProvider).getDialog(activity!!)
//        Mockito.verify(dialog).show()
//    }

    @Test
    fun onGPSActivatedShouldAskForLocation() {
        defaultLocationProvider.onGPSActivated()
        Mockito.verify(defaultLocationProvider).askForLocation(GPS_PROVIDER)
    }

    @Test
fun locationByNetworkShouldAskForLocationWhenNetworkIsAvailable() {
            enableLocationProvider()
            defaultLocationProvider.locationByNetwork
            Mockito.verify(defaultLocationProvider).askForLocation(LocationManager.NETWORK_PROVIDER)
        }

    @Test
fun locationByNetworkShouldFailWhenNetworkIsNotAvailable() {
            disableLocationProvider()
            defaultLocationProvider!!.locationByNetwork
            Mockito.verify(locationListener).onLocationFailed(FailType.NETWORK_NOT_AVAILABLE)
        }

    @Test
    fun askForLocationShouldStopSwitchTasks() {
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        Mockito.verify(continuousTask).stop()
    }

    @Test
    fun askForLocationShouldCheckLastKnowLocation() {
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        Mockito.verify(defaultLocationProvider).checkForLastKnowLocation()
    }

    @Test
    fun askForLocationShouldNotifyProcessChangeRequestLocationUpdateDelayTaskWhenLastLocationIsNotSufficient() {
        val ONE_SECOND: Long = 1000
        Mockito.`when`(defaultLocationSource!!.providerSwitchTask).thenReturn(continuousTask)
        Mockito.`when`(defaultProviderConfiguration!!.gpsWaitPeriod()).thenReturn(ONE_SECOND)
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        Mockito.verify(defaultLocationProvider).notifyProcessChange()
        Mockito.verify(defaultLocationProvider).requestUpdateLocation()
        Mockito.verify(continuousTask).delayed(ONE_SECOND)
    }

    @Test
    fun askForLocationShouldNotifyProcessChangeAndRequestLocationUpdateWhenKeepTrackingIsTrue() {
        val location = Location(GPS_PROVIDER)
        Mockito.`when`(defaultProviderConfiguration!!.acceptableAccuracy()).thenReturn(1f)
        Mockito.`when`(defaultProviderConfiguration!!.acceptableTimePeriod()).thenReturn(1L)
        Mockito.`when`(defaultLocationSource!!.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location)
        Mockito.`when`(defaultLocationSource!!.isLocationSufficient(location, 1L, 1f)).thenReturn(true)
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(true)
        defaultLocationProvider!!.askForLocation(GPS_PROVIDER)
        Mockito.verify(defaultLocationProvider).notifyProcessChange()
        Mockito.verify(defaultLocationProvider).requestUpdateLocation()
    }

    @Test
    fun checkForLastKnownLocationShouldReturnFalse() {
        Assertions.assertThat(defaultLocationProvider!!.checkForLastKnowLocation()).isFalse
    }

    @Test
    fun checkForLastKnownLocationShouldCallOnLocationReceivedAndReturnTrueWhenSufficient() {
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        val location = Location(GPS_PROVIDER)
        Mockito.`when`(defaultProviderConfiguration!!.acceptableAccuracy()).thenReturn(1f)
        Mockito.`when`(defaultProviderConfiguration!!.acceptableTimePeriod()).thenReturn(1L)
        Mockito.`when`(defaultLocationSource!!.getLastKnownLocation(GPS_PROVIDER)).thenReturn(location)
        Mockito.`when`(defaultLocationSource!!.isLocationSufficient(location, 1L, 1f)).thenReturn(true)
        Assertions.assertThat(defaultLocationProvider!!.checkForLastKnowLocation()).isTrue
        Mockito.verify(locationListener).onLocationChanged(location)
    }

    @Test
    fun notifyProcessChangeShouldNotifyWithCorrespondingTypeForProvider() {
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        defaultLocationProvider!!.notifyProcessChange()
        Mockito.verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER)
        defaultLocationProvider!!.setCurrentProvider(NETWORK_PROVIDER)
        defaultLocationProvider!!.notifyProcessChange()
        Mockito.verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER)
    }

    @Test
    fun requestUpdateLocationShouldRunUpdateLocationTaskWithCurrentProvider() {
        val timeInterval: Long = 100
        val distanceInterval: Long = 200
        Mockito.`when`(defaultProviderConfiguration!!.requiredTimeInterval()).thenReturn(timeInterval)
        Mockito.`when`(defaultProviderConfiguration!!.requiredDistanceInterval()).thenReturn(distanceInterval)
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        defaultLocationProvider!!.requestUpdateLocation()
        Mockito.verify(updateRequest).run(GPS_PROVIDER, timeInterval, distanceInterval.toFloat())
    }

    @Test
fun waitPeriodShouldReturnCorrespondingTimeForProvider() {
            val gpsWaitPeriod: Long = 100
            val networkWaitPeriod: Long = 200
            Mockito.`when`(defaultProviderConfiguration!!.gpsWaitPeriod()).thenReturn(gpsWaitPeriod)
            Mockito.`when`(defaultProviderConfiguration!!.networkWaitPeriod()).thenReturn(networkWaitPeriod)
            defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
            Assertions.assertThat(defaultLocationProvider!!.waitPeriod).isEqualTo(gpsWaitPeriod)
            defaultLocationProvider!!.setCurrentProvider(NETWORK_PROVIDER)
            Assertions.assertThat(defaultLocationProvider!!.waitPeriod).isEqualTo(networkWaitPeriod)
        }

    @Test
    fun onLocationReceivedShouldNotifyListenerAndSetWaitingFalse() {
        defaultLocationProvider!!.isWaiting = true
        defaultLocationProvider!!.onLocationReceived(DUMMY_LOCATION)
        Mockito.verify(locationListener).onLocationChanged(DUMMY_LOCATION)
        Assertions.assertThat(defaultLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun onLocationFailedShouldNotifyListenerAndSetWaitingFalse() {
        defaultLocationProvider!!.isWaiting = true
        defaultLocationProvider!!.onLocationFailed(FailType.UNKNOWN)
        Mockito.verify(locationListener).onLocationFailed(FailType.UNKNOWN)
        Assertions.assertThat(defaultLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun onLocationChangedShouldPassLocationToReceived() {
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        Mockito.verify(defaultLocationProvider).onLocationReceived(DUMMY_LOCATION)
    }

    @Test
    fun onLocationChangedShouldStopSwitchTask() {
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        Mockito.verify(continuousTask).stop()
    }

    @Test
    fun onLocationChangedShouldNotStopSwitchTaskIfSwitchTaskIsRemoved() {
        Mockito.`when`(defaultLocationSource!!.switchTaskIsRemoved()).thenReturn(true)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        Mockito.verify(continuousTask, Mockito.never()).stop()
    }

    @Test
    fun onLocationChangedShouldReturnIfUpdateRequestIsRemoved() {
        Mockito.`when`(defaultLocationSource!!.updateRequestIsRemoved()).thenReturn(true)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        Mockito.verify(defaultLocationProvider, Mockito.never()).onLocationReceived(DUMMY_LOCATION)
    }

    @Test
    fun onLocationChangedShouldRemoveUpdatesWhenKeepTrackingFalse() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(false)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        Mockito.verify(defaultLocationSource).removeLocationUpdates(defaultLocationProvider!!)
        Mockito.verify(updateRequest).release()
    }

    @Test
    fun onLocationChangedShouldNotRemoveUpdatesWhenKeepTrackingTrue() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(true)
        defaultLocationProvider!!.onLocationChanged(DUMMY_LOCATION)
        Mockito.verify(defaultLocationSource, Mockito.never()).removeLocationUpdates(defaultLocationProvider!!)
        Mockito.verify(updateRequest, Mockito.never()).release()
    }

    @Test
    fun onStatusChangedShouldRedirectToListener() {
        defaultLocationProvider!!.onStatusChanged(GPS_PROVIDER, 1, null)
        Mockito.verify(locationListener).onStatusChanged(GPS_PROVIDER, 1, null)
    }

    @Test
    fun onProviderEnabledShouldRedirectToListener() {
        defaultLocationProvider!!.onProviderEnabled(GPS_PROVIDER)
        Mockito.verify(locationListener).onProviderEnabled(GPS_PROVIDER)
    }

    @Test
    fun onProviderDisabledShouldRedirectToListener() {
        defaultLocationProvider!!.onProviderDisabled(GPS_PROVIDER)
        Mockito.verify(locationListener).onProviderDisabled(GPS_PROVIDER)
    }

    @Test
    fun runScheduledTaskShouldReleaseUpdateRequest() {
        defaultLocationProvider!!.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK)
        Mockito.verify(updateRequest).release()
    }

    @Test
    fun runScheduledTaskShouldGetLocationByNetworkWhenCurrentProviderIsGPS() {
        defaultLocationProvider!!.setCurrentProvider(GPS_PROVIDER)
        defaultLocationProvider!!.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK)
        Mockito.verify(defaultLocationProvider).locationByNetwork
    }

    @Test
    fun runScheduledTaskShouldFailWithTimeoutWhenCurrentProviderIsNetwork() {
        defaultLocationProvider!!.setCurrentProvider(NETWORK_PROVIDER)
        defaultLocationProvider!!.runScheduledTask(DefaultLocationSource.PROVIDER_SWITCH_TASK)
        Mockito.verify(locationListener).onLocationFailed(FailType.TIMEOUT)
    }

    @Test
    fun onPositiveButtonClickShouldFailWhenThereIsNoActivityOrFragment() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(null)
        defaultLocationProvider!!.onPositiveButtonClick()
        Mockito.verify(locationListener).onLocationFailed(FailType.VIEW_NOT_REQUIRED_TYPE)
    }

    @Test
    fun onNegativeButtonClickShouldGetLocationByNetwork() {
        defaultLocationProvider!!.onNegativeButtonClick()
        Mockito.verify(defaultLocationProvider).locationByNetwork
    }

    private fun enableLocationProvider() {
        Mockito.`when`(defaultLocationSource!!.isProviderEnabled(ArgumentMatchers.anyString())).thenReturn(true)
    }

    private fun disableLocationProvider() {
        Mockito.`when`(defaultLocationSource!!.isProviderEnabled(ArgumentMatchers.anyString())).thenReturn(false)
    }

    companion object {
        private const val GPS_PROVIDER = LocationManager.GPS_PROVIDER
        private const val NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER
        private val DUMMY_LOCATION = Location("")
    }
}