package bazaar.tech.library.location.locationmanager.providers.locationprovider

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.Location
import bazaar.tech.library.location.configuration.GooglePlayServicesConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.listener.FallbackListener
import bazaar.tech.library.location.listener.LocationListener
import bazaar.tech.library.location.locationmanager.fakes.FakeSimpleTask
import bazaar.tech.library.location.providers.locationprovider.GooglePlayServicesLocationProvider
import bazaar.tech.library.location.providers.locationprovider.GooglePlayServicesLocationSource
import bazaar.tech.library.location.view.ContextProcessor
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

class GooglePlayServicesLocationProviderTest {
    @Mock
    lateinit var mockedSource: GooglePlayServicesLocationSource

    @Mock
    lateinit var location: Location

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var googlePlayServicesConfiguration: GooglePlayServicesConfiguration

    @Mock
    lateinit var fallbackListener: FallbackListener

    private lateinit var googlePlayServicesLocationProvider: GooglePlayServicesLocationProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        googlePlayServicesLocationProvider = Mockito.spy(GooglePlayServicesLocationProvider(fallbackListener))
        googlePlayServicesLocationProvider.configure(contextProcessor!!, locationConfiguration!!, locationListener)
        googlePlayServicesLocationProvider.setDispatcherLocationSource(mockedSource)
        Mockito.`when`(locationConfiguration!!.googlePlayServicesConfiguration()).thenReturn(googlePlayServicesConfiguration)
        Mockito.`when`(contextProcessor!!.context).thenReturn(context)
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
    }

    @Test
    fun onResumeShouldNotRequestLocationUpdateWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue()
        googlePlayServicesLocationProvider!!.onResume()
        Mockito.verify(mockedSource, Mockito.never()).requestLocationUpdate()
    }

    @Test
    fun onResumeShouldNotRequestLocationUpdateWhenLocationIsAlreadyProvidedAndNotRequiredToKeepTracking() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(false)
        googlePlayServicesLocationProvider!!.onResume()
        Mockito.verify(mockedSource, Mockito.never()).requestLocationUpdate()
    }

    @Test
    fun onResumeShouldRequestLocationUpdateWhenLocationIsNotYetProvided() {
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider!!.isWaiting = true
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(mockedSource).requestLocationUpdate()
    }

    @Test
    fun onResumeShouldRequestLocationUpdateWhenLocationIsAlreadyProvidedButRequiredToKeepTracking() {
        googlePlayServicesLocationProvider!!.isWaiting = true
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(true)
        googlePlayServicesLocationProvider!!.onResume()
        Mockito.verify(mockedSource).requestLocationUpdate()
    }

    @Test
    fun onPauseShouldNotRemoveLocationUpdatesWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue()
        googlePlayServicesLocationProvider!!.onPause()
        Mockito.verify(mockedSource, Mockito.never()).requestLocationUpdate()
        Mockito.verify(mockedSource, Mockito.never()).removeLocationUpdates()
    }

    @Test
    fun onPauseShouldRemoveLocationUpdates() {
        googlePlayServicesLocationProvider!!.onPause()
        Mockito.verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onDestroyShouldRemoveLocationUpdates() {
        googlePlayServicesLocationProvider!!.onDestroy()
        Mockito.verify(mockedSource).removeLocationUpdates()
    }

    @Test
fun isDialogShownShouldReturnFalseWhenSettingsApiDialogIsNotShown() {
            Assertions.assertThat(googlePlayServicesLocationProvider!!.isDialogShowing).isFalse
        }

    @Test
fun isDialogShownShouldReturnTrueWhenSettingsApiDialogShown() {
            makeSettingsDialogIsOnTrue()
            Assertions.assertThat(googlePlayServicesLocationProvider!!.isDialogShowing).isTrue
        }

    @Test
fun shouldSetWaitingTrue() {
            Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isFalse
            googlePlayServicesLocationProvider!!.get()
            Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isTrue
        }

    @Test
fun shouldFailWhenThereIsNoContext() {
            Mockito.`when`(contextProcessor!!.context).thenReturn(null)
            googlePlayServicesLocationProvider!!.get()
            Mockito.verify(locationListener).onLocationFailed(FailType.VIEW_DETACHED)
        }

    @Test
fun shouldRequestLastLocation() {
            googlePlayServicesLocationProvider!!.get()
            Mockito.verify(mockedSource).requestLastLocation()
        }

    @Test
fun shouldNotRequestLastLocationWhenIgnore() {
            Mockito.`when`(googlePlayServicesConfiguration!!.ignoreLastKnowLocation()).thenReturn(true)
            googlePlayServicesLocationProvider!!.get()
            Mockito.verify(mockedSource, Mockito.never()).requestLastLocation()
        }

    @Test
    fun onLastKnowLocationTaskReceivedShouldNotCallLocationRequiredWhenLastKnowIsReadyAndNoNeedToKeepTracking() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(false)
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(location)
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(googlePlayServicesLocationProvider, Mockito.never()).locationRequired()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldCallRequestLocationUpdateWhenLastLocationIsNull() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(false)
        Mockito.`when`(googlePlayServicesConfiguration!!.ignoreLastKnowLocation()).thenReturn(false)
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(googlePlayServicesLocationProvider).locationRequired()
        Mockito.verify(googlePlayServicesLocationProvider).requestLocationUpdate()
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldCallLocationRequiredWhenLastKnowIsNotAvailable() {
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(googlePlayServicesLocationProvider).locationRequired()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldNotifyOnLocationChangedWhenLocationIsAvailable() {
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(location)
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(googlePlayServicesLocationProvider).onLocationChanged(location!!)
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldInvokeLocationRequiredWhenKeepTrackingIsTrue() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(true)
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(location)
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(googlePlayServicesLocationProvider).onLocationChanged(location!!)
        Mockito.verify(googlePlayServicesLocationProvider).locationRequired()
    }

    @Test
    fun onLastKnowLocationTaskReceivedShouldInvokeRequestLocationFalseWhenLastKnownLocationIsNull() {
        val lastLocationTask = FakeSimpleTask<Location>()
        lastLocationTask.success(null)
        googlePlayServicesLocationProvider!!.onLastKnowLocationTaskReceived(lastLocationTask as Task<Location>)
        Mockito.verify(googlePlayServicesLocationProvider).locationRequired()
    }

    @Test
    fun cancelShouldRemoveLocationRequestWhenInvokeCancel() {
        googlePlayServicesLocationProvider!!.cancel()
        Mockito.verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onActivityResultShouldSetDialogShownToFalse() {
        makeSettingsDialogIsOnTrue()
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isDialogShowing).isTrue
        googlePlayServicesLocationProvider!!.onActivityResult(RequestCode.SETTINGS_API, -1, null)
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isDialogShowing).isFalse
    }

    @Test
    fun onActivityResultShouldRequestLocationUpdateWhenResultIsOk() {
        googlePlayServicesLocationProvider!!.onActivityResult(RequestCode.SETTINGS_API, Activity.RESULT_OK, null)
        Mockito.verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun onActivityResultShouldCallSettingsApiFailWhenResultIsNotOk() {
        googlePlayServicesLocationProvider!!.onActivityResult(RequestCode.SETTINGS_API, Activity.RESULT_CANCELED, null)
        Mockito.verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
    }

    @Test
    fun onLocationChangedShouldNotifyListener() {
        googlePlayServicesLocationProvider!!.onLocationChanged(location!!)
        Mockito.verify(locationListener).onLocationChanged(location)
        Mockito.verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onLocationChangedShouldSetWaitingFalse() {
        googlePlayServicesLocationProvider!!.isWaiting = true
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isTrue
        googlePlayServicesLocationProvider!!.onLocationChanged(location!!)
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun onLocationChangedShouldRemoveUpdateLocationWhenKeepTrackingIsNotRequired() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(false)
        googlePlayServicesLocationProvider!!.onLocationChanged(location!!)
        Mockito.verify(mockedSource).removeLocationUpdates()
    }

    @Test
    fun onLocationChangedShouldNotRemoveUpdateLocationWhenKeepTrackingIsRequired() {
        Mockito.`when`(locationConfiguration!!.keepTracking()).thenReturn(true)
        googlePlayServicesLocationProvider!!.onLocationChanged(location!!)
        Mockito.verify(mockedSource, Mockito.never()).removeLocationUpdates()
    }

    @Test
    fun onResultShouldCallSettingsApiFailWhenChangeUnavailable() {
        googlePlayServicesLocationProvider
                .onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE))
        Mockito.verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
    }

    @Test
    fun onResultShouldCallSettingsApiFailWithSettingsDeniedWhenOtherCase() {
        val settingsResultWith = getSettingsResultWithError(LocationSettingsStatusCodes.CANCELED)
        googlePlayServicesLocationProvider!!.onFailure(settingsResultWith)
        Mockito.verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED)
    }

    @Test
    fun resolveSettingsApiShouldCallSettingsApiFailWhenThereIsNoActivity() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(null)
        googlePlayServicesLocationProvider!!.resolveSettingsApi(ResolvableApiException(Status(1)))
        Mockito.verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE)
    }

    @Test
    @Throws(Exception::class)
    fun resolveSettingsApiShouldStartSettingsApiResolutionForResult() {
        val status = Status(1)
        val resolvable = ResolvableApiException(status)
        googlePlayServicesLocationProvider!!.resolveSettingsApi(resolvable)
        Mockito.verify(mockedSource).startSettingsApiResolutionForResult(resolvable, activity!!)
    }

    @Test
    @Throws(Exception::class)
    fun resolveSettingsApiShouldCallSettingsApiFailWhenExceptionThrown() {
        val status = Status(1)
        val resolvable = ResolvableApiException(status)
        Mockito.doThrow(SendIntentException()).`when`(mockedSource).startSettingsApiResolutionForResult(resolvable, activity!!)
        googlePlayServicesLocationProvider!!.resolveSettingsApi(resolvable)
        Mockito.verify(googlePlayServicesLocationProvider).settingsApiFail(FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG)
    }

    @Test
    fun locationRequiredShouldCheckLocationSettingsWhenConfigurationAsksForSettingsApi() {
        Mockito.`when`(googlePlayServicesConfiguration!!.askForSettingsApi()).thenReturn(true)
        googlePlayServicesLocationProvider!!.locationRequired()
        Mockito.verify(mockedSource).checkLocationSettings()
    }

    @Test
    fun locationRequiredShouldRequestLocationUpdateWhenConfigurationDoesntRequireToAskForSettingsApi() {
        googlePlayServicesLocationProvider!!.locationRequired()
        Mockito.verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun requestLocationUpdateShouldUpdateProcessTypeOnListener() {
        googlePlayServicesLocationProvider!!.requestLocationUpdate()
        Mockito.verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES)
    }

    @Test
    fun requestLocationUpdateShouldRequest() {
        googlePlayServicesLocationProvider!!.requestLocationUpdate()
        Mockito.verify(mockedSource).requestLocationUpdate()
    }

    @Test
    fun settingsApiFailShouldCallFailWhenConfigurationFailOnSettingsApiSuspendedTrue() {
        Mockito.`when`(googlePlayServicesConfiguration!!.failOnSettingsApiSuspended()).thenReturn(true)
        googlePlayServicesLocationProvider!!.settingsApiFail(FailType.UNKNOWN)
        Mockito.verify(googlePlayServicesLocationProvider).failed(FailType.UNKNOWN)
    }

    @Test
    fun settingsApiFailShouldCallRequestLocationUpdateWhenConfigurationFailOnSettingsApiSuspendedFalse() {
        Mockito.`when`(googlePlayServicesConfiguration!!.failOnSettingsApiSuspended()).thenReturn(false)
        googlePlayServicesLocationProvider!!.settingsApiFail(FailType.UNKNOWN)
        Mockito.verify(googlePlayServicesLocationProvider).requestLocationUpdate()
    }

    @Test
    fun failedShouldRedirectToListenerWhenFallbackToDefaultIsFalse() {
        Mockito.`when`(googlePlayServicesConfiguration!!.fallbackToDefault()).thenReturn(false)
        googlePlayServicesLocationProvider!!.failed(FailType.UNKNOWN)
        Mockito.verify(locationListener).onLocationFailed(FailType.UNKNOWN)
    }

    @Test
    fun failedShouldCallFallbackWhenFallbackToDefaultIsTrue() {
        Mockito.`when`(googlePlayServicesConfiguration!!.fallbackToDefault()).thenReturn(true)
        googlePlayServicesLocationProvider!!.failed(FailType.UNKNOWN)
        Mockito.verify(fallbackListener).onFallback()
    }

    @Test
    fun failedShouldSetWaitingFalse() {
        googlePlayServicesLocationProvider!!.isWaiting = true
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isTrue
        googlePlayServicesLocationProvider!!.failed(FailType.UNKNOWN)
        Assertions.assertThat(googlePlayServicesLocationProvider!!.isWaiting).isFalse
    }

    private fun makeSettingsDialogIsOnTrue() {
        googlePlayServicesLocationProvider!!.onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.RESOLUTION_REQUIRED))
    }

    companion object {
        private fun getSettingsResultWithSuccess(statusCode: Int): LocationSettingsResponse {
            val status = Status(statusCode, null, null)
            val result = LocationSettingsResponse()
            result.setResult(LocationSettingsResult(status, null))
            return result
        }

        private fun getSettingsResultWithError(statusCode: Int): Exception {
            val status = Status(statusCode, null, null)
            return if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                ResolvableApiException(status)
            } else {
                ApiException(status)
            }
        }
    }
}