package bazaar.tech.library.location.locationmanager.configuration

import bazaar.tech.library.location.configuration.DefaultProviderConfiguration
import bazaar.tech.library.location.constants.ProviderType
import bazaar.tech.library.location.locationmanager.fakes.MockDialogProvider
import bazaar.tech.library.location.providers.dialogprovider.SimpleMessageDialogProvider
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class DefaultProviderConfigurationTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()
    @Test
    fun checkDefaultValues() {
        val configuration = DefaultProviderConfiguration.Builder().build()
        Assertions.assertThat(configuration.requiredTimeInterval()).isEqualTo((5 * MINUTE).toLong())
        Assertions.assertThat(configuration.requiredDistanceInterval()).isEqualTo(0)
        Assertions.assertThat(configuration.acceptableAccuracy()).isEqualTo(5.0f)
        Assertions.assertThat(configuration.acceptableTimePeriod()).isEqualTo((5 * MINUTE).toLong())
        Assertions.assertThat(configuration.gpsWaitPeriod()).isEqualTo((20 * SECOND).toLong())
        Assertions.assertThat(configuration.networkWaitPeriod()).isEqualTo((20 * SECOND).toLong())
        Assertions.assertThat(configuration.gpsDialogProvider()).isNull()
    }

    @Test
    fun requiredTimeIntervalShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredTimeInterval"))
        DefaultProviderConfiguration.Builder().requiredTimeInterval(-1)
    }

    @Test
    fun requiredDistanceIntervalShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredDistanceInterval"))
        DefaultProviderConfiguration.Builder().requiredDistanceInterval(-1)
    }

    @Test
    fun acceptableAccuracyShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("acceptableAccuracy"))
        DefaultProviderConfiguration.Builder().acceptableAccuracy(-1f)
    }

    @Test
    fun acceptableTimePeriodShouldThrowExceptionWhenNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("acceptableTimePeriod"))
        DefaultProviderConfiguration.Builder().acceptableTimePeriod(-1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenNetworkWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.NETWORK, -1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenGPSWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.GPS, -1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenDefaultProvidersWaitPeriodIsNegative() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.DEFAULT_PROVIDERS, -1)
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenGooglePlayServicesWaitPeriodIsSet() {
        expectedException.expect(IllegalStateException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("GooglePlayServices"))
        DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 1)
    }

    @Test
    fun setWaitPeriodShouldSetPeriodsWhenDefaultProvidersIsSet() {
        val providerConfiguration = DefaultProviderConfiguration.Builder().setWaitPeriod(ProviderType.DEFAULT_PROVIDERS, 1).build()
        Assertions.assertThat(providerConfiguration.gpsWaitPeriod()).isEqualTo(1)
        Assertions.assertThat(providerConfiguration.networkWaitPeriod()).isEqualTo(1)
    }

    @Test
    fun whenGpsMessageAndDialogProviderNotSetAskForGPSEnableShouldReturnFalse() {
        val configuration = DefaultProviderConfiguration.Builder().build()
        Assertions.assertThat(configuration.askForEnableGPS()).isFalse
    }

    @Test
    fun whenGpsMessageSetAskForGPSEnableShouldReturnTrue() {
        val configuration = DefaultProviderConfiguration.Builder()
                .gpsMessage("some_text")
                .build()
        Assertions.assertThat(configuration.askForEnableGPS()).isTrue
    }

    @Test
    fun whenDialogProviderSetAskForGPSEnableShouldReturnTrue() {
        val configuration = DefaultProviderConfiguration.Builder()
                .gpsDialogProvider(MockDialogProvider("some_text"))
                .build()
        Assertions.assertThat(configuration.askForEnableGPS()).isTrue
    }

    @Test
    fun whenGpsMessageIsEmptyAndDialogProviderIsNotSetThenDialogProviderShouldBeNull() {
        val configuration = DefaultProviderConfiguration.Builder().build()
        Assertions.assertThat(configuration.gpsDialogProvider()).isNull()
    }

    @Test
    fun whenGpsMessageIsNotEmptyDefaultDialogProviderShouldBeSimple() {
        val GPS_MESSAGE = "some_text"
        val configuration = DefaultProviderConfiguration.Builder()
                .gpsMessage(GPS_MESSAGE)
                .build()
        Assertions.assertThat(configuration.gpsDialogProvider())
                .isNotNull
                .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        Assertions.assertThat((configuration.gpsDialogProvider() as SimpleMessageDialogProvider?)!!.message()).isEqualTo(GPS_MESSAGE)
    }

    @Test
    fun whenDialogProviderIsSetMessageShouldBeIgnored() {
        val GPS_MESSAGE = "some_text"
        val configuration = DefaultProviderConfiguration.Builder()
                .gpsMessage("ignored_message")
                .gpsDialogProvider(MockDialogProvider(GPS_MESSAGE))
                .build()
        Assertions.assertThat(configuration.gpsDialogProvider())
                .isNotNull
                .isExactlyInstanceOf(MockDialogProvider::class.java)
        Assertions.assertThat((configuration.gpsDialogProvider() as MockDialogProvider?)!!.message()).isEqualTo(GPS_MESSAGE)
    }

    @Test
    fun clonesShouldShareSameInstances() {
        val configuration = DefaultProviderConfiguration.Builder()
                .gpsDialogProvider(MockDialogProvider("some_text"))
                .build()
        val firstClone = configuration.newBuilder().build()
        val secondClone = configuration.newBuilder().build()
        Assertions.assertThat(firstClone.requiredTimeInterval())
                .isEqualTo(secondClone.requiredTimeInterval())
                .isEqualTo((5 * MINUTE).toLong())
        Assertions.assertThat(firstClone.requiredDistanceInterval())
                .isEqualTo(secondClone.requiredDistanceInterval())
                .isEqualTo(0)
        Assertions.assertThat(firstClone.acceptableAccuracy())
                .isEqualTo(secondClone.acceptableAccuracy())
                .isEqualTo(5.0f)
        Assertions.assertThat(firstClone.acceptableTimePeriod())
                .isEqualTo(secondClone.acceptableTimePeriod())
                .isEqualTo((5 * MINUTE).toLong())
        Assertions.assertThat(firstClone.gpsWaitPeriod())
                .isEqualTo(secondClone.gpsWaitPeriod())
                .isEqualTo((20 * SECOND).toLong())
        Assertions.assertThat(firstClone.networkWaitPeriod())
                .isEqualTo(secondClone.networkWaitPeriod())
                .isEqualTo((20 * SECOND).toLong())
        Assertions.assertThat(firstClone.gpsDialogProvider())
                .isEqualTo(secondClone.gpsDialogProvider())
                .isNotNull
                .isExactlyInstanceOf(MockDialogProvider::class.java)
    }

    companion object {
        private const val SECOND = 1000
        private const val MINUTE = 60 * SECOND
    }
}