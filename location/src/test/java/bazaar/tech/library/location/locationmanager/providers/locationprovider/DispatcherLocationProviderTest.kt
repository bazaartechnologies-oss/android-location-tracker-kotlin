package bazaar.tech.library.location.locationmanager.providers.locationprovider

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import bazaar.tech.library.location.configuration.DefaultProviderConfiguration
import bazaar.tech.library.location.configuration.GooglePlayServicesConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.helper.continuoustask.ContinuousTask
import bazaar.tech.library.location.listener.LocationListener
import bazaar.tech.library.location.providers.locationprovider.DefaultLocationProvider
import bazaar.tech.library.location.providers.locationprovider.DispatcherLocationProvider
import bazaar.tech.library.location.providers.locationprovider.DispatcherLocationSource
import bazaar.tech.library.location.providers.locationprovider.GooglePlayServicesLocationProvider
import bazaar.tech.library.location.view.ContextProcessor
import com.google.android.gms.common.ConnectionResult
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class DispatcherLocationProviderTest {
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
    lateinit var locationConfiguration: LocationConfiguration

    @Mock
    lateinit var googlePlayServicesConfiguration: GooglePlayServicesConfiguration

    @Mock
    lateinit var defaultProviderConfiguration: DefaultProviderConfiguration

    @Mock
    lateinit var dispatcherLocationSource: DispatcherLocationSource

    @Mock
    lateinit var defaultLocationProvider: DefaultLocationProvider

    @Mock
    lateinit var googlePlayServicesLocationProvider: GooglePlayServicesLocationProvider

    @Mock
    lateinit var continuousTask: ContinuousTask

    private lateinit var dispatcherLocationProvider: DispatcherLocationProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        dispatcherLocationProvider = Mockito.spy(DispatcherLocationProvider())
        dispatcherLocationProvider.configure(contextProcessor!!, locationConfiguration!!, locationListener)
        dispatcherLocationProvider.setDispatcherLocationSource(dispatcherLocationSource)
        Mockito.`when`(locationConfiguration!!.defaultProviderConfiguration()).thenReturn(defaultProviderConfiguration)
        Mockito.`when`(locationConfiguration!!.googlePlayServicesConfiguration()).thenReturn(googlePlayServicesConfiguration)
        Mockito.`when`(googlePlayServicesConfiguration!!.googlePlayServicesWaitPeriod()).thenReturn(GOOGLE_PLAY_SERVICES_SWITCH_PERIOD)
        Mockito.`when`(dispatcherLocationSource!!.createDefaultLocationProvider()).thenReturn(defaultLocationProvider)
        Mockito.`when`(dispatcherLocationSource!!.createGooglePlayServicesLocationProvider(dispatcherLocationProvider))
                .thenReturn(googlePlayServicesLocationProvider)
        Mockito.`when`(dispatcherLocationSource!!.gpServicesSwitchTask()).thenReturn(continuousTask)
        Mockito.`when`(contextProcessor!!.context).thenReturn(context)
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
    }

    @Test
    fun onPauseShouldRedirectToActiveProvider() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        dispatcherLocationProvider!!.onPause()
        Mockito.verify(defaultLocationProvider).onPause()
        Mockito.verify(continuousTask).pause()
    }

    @Test
    fun onResumeShouldRedirectToActiveProvider() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        dispatcherLocationProvider!!.onResume()
        Mockito.verify(defaultLocationProvider).onResume()
        Mockito.verify(continuousTask).resume()
    }

    @Test
    fun onDestroyShouldRedirectToActiveProvider() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        dispatcherLocationProvider!!.onDestroy()
        Mockito.verify(defaultLocationProvider).onDestroy()
        Mockito.verify(continuousTask).stop()
    }

    @Test
    fun cancelShouldRedirectToActiveProvider() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        dispatcherLocationProvider!!.cancel()
        Mockito.verify(defaultLocationProvider).cancel()
        Mockito.verify(continuousTask).stop()
    }

    @Test
    fun isWaitingShouldReturnFalseWhenNoActiveProvider() {
        Assertions.assertThat(dispatcherLocationProvider!!.isWaiting).isFalse
    }

    @Test
    fun isWaitingShouldRetrieveFromActiveProvider() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        Mockito.`when`(defaultLocationProvider!!.isWaiting).thenReturn(true)
        Assertions.assertThat(dispatcherLocationProvider!!.isWaiting).isTrue
        Mockito.verify(defaultLocationProvider).isWaiting
    }

    @Test
    fun isDialogShowingShouldReturnFalseWhenNoDialogShown() {
        Assertions.assertThat(dispatcherLocationProvider!!.isDialogShowing).isFalse
    }

    // so dialog is not null
    @Test
    fun isDialogShowingShouldReturnTrueWhenGpServicesIsShowing() {
        showGpServicesDialogShown() // so dialog is not null
        Mockito.`when`(dialog!!.isShowing).thenReturn(true)
        Assertions.assertThat(dispatcherLocationProvider!!.isDialogShowing).isTrue
    }

    // so provider is not null
    @Test
    fun isDialogShowingShouldRetrieveFromActiveProviderWhenExists() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!) // so provider is not null
        Mockito.`when`(defaultLocationProvider!!.isDialogShowing).thenReturn(true)
        Assertions.assertThat(dispatcherLocationProvider!!.isDialogShowing).isTrue
        Mockito.verify(defaultLocationProvider).isDialogShowing
    }

    @Test
    fun runScheduledTaskShouldDoNothingWhenActiveProviderIsNotGPServices() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        Mockito.verify(defaultLocationProvider).configure(dispatcherLocationProvider!!)
        dispatcherLocationProvider!!.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK)
        Mockito.verifyNoMoreInteractions(defaultLocationProvider)
    }

    @Test
    fun runScheduledTaskShouldDoNothingWhenNoOnGoingTask() {
        dispatcherLocationProvider!!.setLocationProvider(googlePlayServicesLocationProvider!!)
        Mockito.verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider!!)
        Mockito.`when`(googlePlayServicesLocationProvider!!.isWaiting).thenReturn(false)
        dispatcherLocationProvider!!.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK)
        Mockito.verify(googlePlayServicesLocationProvider).isWaiting
        Mockito.verifyNoMoreInteractions(googlePlayServicesLocationProvider)
    }

    @Test
    fun runScheduledTaskShouldCancelCurrentProviderAndRunWithDefaultWhenGpServicesTookEnough() {
        dispatcherLocationProvider!!.setLocationProvider(googlePlayServicesLocationProvider!!)
        Mockito.`when`(googlePlayServicesLocationProvider!!.isWaiting).thenReturn(true)
        dispatcherLocationProvider!!.runScheduledTask(DispatcherLocationSource.GOOGLE_PLAY_SERVICE_SWITCH_TASK)
        Mockito.verify(dispatcherLocationProvider).cancel()
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun onActivityResultShouldRedirectToActiveProvider() {
        val data = Intent()
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        dispatcherLocationProvider!!.onActivityResult(-1, -1, data)
        Mockito.verify(defaultLocationProvider).onActivityResult(ArgumentMatchers.eq(-1), ArgumentMatchers.eq(-1), ArgumentMatchers.eq(data))
    }

    @Test
    fun onActivityResultShouldCallCheckGooglePlayServicesAvailabilityWithFalseWhenRequestCodeMatches() {
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        dispatcherLocationProvider!!.onActivityResult(RequestCode.GOOGLE_PLAY_SERVICES, -1, null)
        Mockito.verify(dispatcherLocationProvider).checkGooglePlayServicesAvailability(ArgumentMatchers.eq(false))
    }

    @Test
    fun shouldContinueWithDefaultProviderIfThereIsNoGpServicesConfiguration() {
        Mockito.`when`(locationConfiguration!!.googlePlayServicesConfiguration()).thenReturn(null)
        dispatcherLocationProvider!!.get()
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun shouldCallCheckGooglePlayServicesAvailabilityWithTrue() {
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        Mockito.`when`(googlePlayServicesConfiguration!!.askForGooglePlayServices()).thenReturn(true)
        dispatcherLocationProvider!!.get()
        Mockito.verify(dispatcherLocationProvider).checkGooglePlayServicesAvailability(ArgumentMatchers.eq(true))
    }

    @Test
    fun onFallbackShouldCallCancelAndContinueWithDefaultProviders() {
        dispatcherLocationProvider!!.onFallback()
        Mockito.verify(dispatcherLocationProvider).cancel()
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun checkGooglePlayServicesAvailabilityShouldGetLocationWhenApiIsAvailable() {
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiAvailable(context)).thenReturn(ConnectionResult.SUCCESS)
        dispatcherLocationProvider!!.checkGooglePlayServicesAvailability(false) // could be also true, wouldn't matter
        Mockito.verify(dispatcherLocationProvider).locationFromGooglePlayServices
    }

    @Test
    fun checkGooglePlayServicesAvailabilityShouldContinueWithDefaultWhenCalledWithFalse() {
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        dispatcherLocationProvider!!.checkGooglePlayServicesAvailability(false)
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun checkGooglePlayServicesAvailabilityShouldAskForGooglePlayServicesWhenCalledWithTrue() {
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        dispatcherLocationProvider!!.checkGooglePlayServicesAvailability(true)
        Mockito.verify(dispatcherLocationProvider).askForGooglePlayServices(ArgumentMatchers.eq(RESOLVABLE_ERROR))
    }

    @Test
    fun askForGooglePlayServicesShouldContinueWithDefaultProvidersWhenErrorNotResolvable() {
        Mockito.`when`(googlePlayServicesConfiguration!!.askForGooglePlayServices()).thenReturn(true)
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiErrorUserResolvable(NOT_RESOLVABLE_ERROR)).thenReturn(false)
        dispatcherLocationProvider!!.askForGooglePlayServices(NOT_RESOLVABLE_ERROR)
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun askForGooglePlayServicesShouldContinueWithDefaultProvidersWhenConfigurationNoRequire() {
        Mockito.`when`(googlePlayServicesConfiguration!!.askForGooglePlayServices()).thenReturn(false)
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true)
        dispatcherLocationProvider!!.askForGooglePlayServices(RESOLVABLE_ERROR)
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun askForGooglePlayServicesShouldResolveGooglePlayServicesWhenPossible() {
        Mockito.`when`(googlePlayServicesConfiguration!!.askForGooglePlayServices()).thenReturn(true)
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true)
        dispatcherLocationProvider!!.askForGooglePlayServices(RESOLVABLE_ERROR)
        Mockito.verify(dispatcherLocationProvider).resolveGooglePlayServices(RESOLVABLE_ERROR)
    }

    @Test
    fun resolveGooglePlayServicesShouldContinueWithDefaultWhenResolveDialogIsNull() {
        Mockito.`when`(dispatcherLocationSource!!.getGoogleApiErrorDialog(ArgumentMatchers.eq(activity), ArgumentMatchers.eq(RESOLVABLE_ERROR),
                ArgumentMatchers.eq(RequestCode.GOOGLE_PLAY_SERVICES), ArgumentMatchers.any(DialogInterface.OnCancelListener::class.java))).thenReturn(null)
        dispatcherLocationProvider!!.resolveGooglePlayServices(RESOLVABLE_ERROR)
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun resolveGooglePlayServicesShouldContinueWithDefaultWhenErrorCannotBeResolved() {
        val unresolvableError = ConnectionResult.SERVICE_INVALID
        val dismissListener = arrayOfNulls<DialogInterface.OnDismissListener>(1)
        Mockito.`when`(dispatcherLocationSource!!.getGoogleApiErrorDialog(ArgumentMatchers.eq(activity), ArgumentMatchers.eq(unresolvableError),
                ArgumentMatchers.eq(RequestCode.GOOGLE_PLAY_SERVICES), ArgumentMatchers.any(DialogInterface.OnCancelListener::class.java))).thenReturn(dialog)

        // catch and store real OnDismissListener listener
        Mockito.doAnswer { invocation ->
            dismissListener[0] = invocation.getArgument(0)
            null
        }.`when`(dialog).setOnDismissListener(ArgumentMatchers.any(DialogInterface.OnDismissListener::class.java))

        // simulate dialog dismiss event
        Mockito.doAnswer {
            dismissListener[0]!!.onDismiss(dialog)
            null
        }.`when`(dialog).dismiss()
        dispatcherLocationProvider!!.resolveGooglePlayServices(unresolvableError)
        Mockito.verify(dialog).show()
        dialog!!.dismiss() // Simulate dismiss dialog (error cannot be resolved)
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun resolveGooglePlayServicesShouldContinueWithDefaultWhenWhenResolveDialogIsCancelled() {
        val cancelListener = arrayOfNulls<DialogInterface.OnCancelListener>(1)

        // catch and store real OnCancelListener listener
        Mockito.doAnswer { invocation ->
            cancelListener[0] = invocation.getArgument(3)
            dialog
        }.`when`(dispatcherLocationSource).getGoogleApiErrorDialog(ArgumentMatchers.eq(activity), ArgumentMatchers.eq(RESOLVABLE_ERROR),
                ArgumentMatchers.eq(RequestCode.GOOGLE_PLAY_SERVICES), ArgumentMatchers.any(DialogInterface.OnCancelListener::class.java))

        // simulate dialog cancel event
        Mockito.doAnswer {
            cancelListener[0]!!.onCancel(dialog)
            null
        }.`when`(dialog).cancel()
        dispatcherLocationProvider!!.resolveGooglePlayServices(RESOLVABLE_ERROR)
        Mockito.verify(dialog).show()
        dialog!!.cancel() // Simulate cancel dialog (user cancelled dialog)
        Mockito.verify(dispatcherLocationProvider).continueWithDefaultProviders()
    }

    @Test
    fun resolveGooglePlayServicesShouldShowDialogWhenResolveDialogNotNull() {
        Mockito.`when`(dispatcherLocationSource!!.getGoogleApiErrorDialog(ArgumentMatchers.eq(activity), ArgumentMatchers.eq(RESOLVABLE_ERROR),
                ArgumentMatchers.eq(RequestCode.GOOGLE_PLAY_SERVICES), ArgumentMatchers.any(DialogInterface.OnCancelListener::class.java))).thenReturn(dialog)
        dispatcherLocationProvider!!.resolveGooglePlayServices(RESOLVABLE_ERROR)
        Mockito.verify(dialog).show()
    }

    @Test
    fun locationFromGooglePlayServices() {
        dispatcherLocationProvider!!.locationFromGooglePlayServices
        Mockito.verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider!!)
        Mockito.verify(continuousTask).delayed(GOOGLE_PLAY_SERVICES_SWITCH_PERIOD)
        Mockito.verify(googlePlayServicesLocationProvider).get()
    }


    @Test
    fun continueWithDefaultProviders() {
        dispatcherLocationProvider!!.continueWithDefaultProviders()
        Mockito.verify(defaultLocationProvider).configure(dispatcherLocationProvider!!)
        Mockito.verify(defaultLocationProvider).get()
    }

    @Test
    fun setLocationProviderShouldConfigureGivenProvider() {
        dispatcherLocationProvider!!.setLocationProvider(defaultLocationProvider!!)
        Mockito.verify(defaultLocationProvider).configure(dispatcherLocationProvider!!)
        dispatcherLocationProvider!!.setLocationProvider(googlePlayServicesLocationProvider!!)
        Mockito.verify(googlePlayServicesLocationProvider).configure(dispatcherLocationProvider!!)
    }

    private fun showGpServicesDialogShown() {
        Mockito.`when`(googlePlayServicesConfiguration!!.askForGooglePlayServices()).thenReturn(true)
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiAvailable(context)).thenReturn(RESOLVABLE_ERROR)
        Mockito.`when`(dispatcherLocationSource!!.isGoogleApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true)
        Mockito.`when`(dispatcherLocationSource!!.getGoogleApiErrorDialog(ArgumentMatchers.eq(activity), ArgumentMatchers.eq(RESOLVABLE_ERROR),
                ArgumentMatchers.eq(RequestCode.GOOGLE_PLAY_SERVICES), ArgumentMatchers.any(DialogInterface.OnCancelListener::class.java))).thenReturn(dialog)
        dispatcherLocationProvider!!.checkGooglePlayServicesAvailability(true)
        Mockito.verify(dialog).show()
    }

    companion object {
        private const val GOOGLE_PLAY_SERVICES_SWITCH_PERIOD = (5 * 1000).toLong()
        private const val RESOLVABLE_ERROR = ConnectionResult.SERVICE_MISSING
        private const val NOT_RESOLVABLE_ERROR = ConnectionResult.INTERNAL_ERROR
    }
}