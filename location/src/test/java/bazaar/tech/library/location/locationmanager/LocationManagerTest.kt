package bazaar.tech.library.location.locationmanager

import android.content.Intent
import bazaar.tech.library.location.LocationManager
import bazaar.tech.library.location.configuration.Defaults
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import bazaar.tech.library.location.listener.LocationListener
import bazaar.tech.library.location.providers.locationprovider.DispatcherLocationProvider
import bazaar.tech.library.location.providers.locationprovider.LocationProvider
import bazaar.tech.library.location.providers.permissionprovider.PermissionProvider
import bazaar.tech.library.location.view.ContextProcessor
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.*
import java.lang.NullPointerException

open class LocationManagerTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var locationListener: LocationListener

    @Mock
    lateinit var locationProvider: LocationProvider

    @Mock
    lateinit var permissionProvider: PermissionProvider

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var locationConfiguration: LocationConfiguration

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(locationConfiguration.permissionConfiguration().permissionProvider()).thenReturn(permissionProvider)
    }

    @Test
    fun buildingWithoutContextProcessorShouldThrowException() {
        expectedException.expect(NullPointerException::class.java)
        LocationManager.Builder((null as ContextProcessor?)!!)
                .locationProvider(locationProvider)
                .notify(locationListener)
                .build()
    }

    // region Build Tests
    @Test
    fun buildingWithoutProviderShouldUseDispatcherLocationProvider() {
        val locationManager = LocationManager.Builder(contextProcessor)
                .configuration(locationConfiguration)
                .notify(locationListener)
                .build()
        Assertions.assertThat(locationManager.activeProvider())
                .isNotNull
                .isExactlyInstanceOf(DispatcherLocationProvider::class.java)
    }

    @Test
    fun buildingShouldCallConfigureAndSetListenerOnProvider() {
        buildLocationManager()
        Mockito.verify(locationProvider).configure(contextProcessor, locationConfiguration, locationListener)
    }


    // endregion
    // region Redirect Tests
    @Test
    fun whenOnPauseShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.onPause()
        Mockito.verify(locationProvider).onPause()
    }

    @Test
    fun whenOnResumeShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.onResume()
        Mockito.verify(locationProvider).onResume()
    }

    @Test
    fun whenOnDestroyShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.onDestroy()
        Mockito.verify(locationProvider).onDestroy()
    }

    @Test
    fun whenCancelShouldRedirectToLocationProvider() {
        val locationManager = buildLocationManager()
        locationManager.cancel()
        Mockito.verify(locationProvider).cancel()
    }

    // endregion
    // region Retrieve Tests
    @Test
    fun isWaitingForLocationShouldRetrieveFromLocationProvider() {
        Mockito.`when`(locationProvider.isWaiting).thenReturn(true)
        val locationManager = buildLocationManager()
        Assertions.assertThat(locationManager.isWaitingForLocation).isTrue
        Mockito.verify(locationProvider).isWaiting
    }

    @Test
    fun isAnyDialogShowingShouldRetrieveFromLocationProvider() {
        Mockito.`when`(locationProvider!!.isDialogShowing).thenReturn(true)
        val locationManager = buildLocationManager()
        Assertions.assertThat(locationManager.isAnyDialogShowing).isTrue
        Mockito.verify(locationProvider).isDialogShowing
    }

    // endregion

    @Test
    fun whenRequestedPermissionsAreGrantedShouldNotifyListenerWithFalse() {
        val locationManager = buildLocationManager()
        Mockito.`when`(permissionProvider!!.permissionListener).thenReturn(locationManager)
        permissionProvider!!.permissionListener!!.onPermissionsGranted()
        Mockito.verify(locationListener).onPermissionGranted(ArgumentMatchers.eq(false))
    }

    @Test
    fun whenRequestedPermissionsAreDeniedShouldCallFailOnListener() {
        val locationManager = buildLocationManager()
        Mockito.`when`(permissionProvider!!.permissionListener).thenReturn(locationManager)
        permissionProvider!!.permissionListener!!.onPermissionsDenied()
        Mockito.verify(locationListener).onLocationFailed(ArgumentMatchers.eq(FailType.PERMISSION_DENIED))
    }

    @Test
    fun whenAskForPermissionShouldNotifyListenerWithProcessTypeChanged() {
        val locationManager = buildLocationManager()
        locationManager.askForPermission()
        Mockito.verify(locationListener).onProcessTypeChanged(ArgumentMatchers.eq(ProcessType.ASKING_PERMISSIONS))
    }

    @Test
    fun whenRequestingPermissionIsNotPossibleThenItShouldFail() {
        Mockito.`when`(permissionProvider!!.requestPermissions()).thenReturn(false)
        val locationManager = buildLocationManager()
        locationManager.askForPermission()
        Mockito.verify(locationListener).onLocationFailed(ArgumentMatchers.eq(FailType.PERMISSION_DENIED))
    }

    private fun buildLocationManager(): LocationManager {
        return LocationManager.Builder(contextProcessor!!)
                .locationProvider(locationProvider!!)
                .configuration(locationConfiguration!!)
                .notify(locationListener)
                .build()
    }
}