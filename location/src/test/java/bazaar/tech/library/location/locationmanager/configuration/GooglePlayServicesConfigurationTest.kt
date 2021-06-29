package bazaar.tech.library.location.locationmanager.configuration

import bazaar.tech.library.location.configuration.GooglePlayServicesConfiguration
import com.google.android.gms.location.LocationRequest
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class GooglePlayServicesConfigurationTest {
    @Rule
    @JvmField
    var expectedException: ExpectedException = ExpectedException.none()
    @Test
    fun checkDefaultValues() {
        val configuration = GooglePlayServicesConfiguration.Builder().build()
        Assertions.assertThat(configuration.locationRequest()).isEqualTo(createDefaultLocationRequest())
        Assertions.assertThat(configuration.fallbackToDefault()).isTrue
        Assertions.assertThat(configuration.askForGooglePlayServices()).isFalse
        Assertions.assertThat(configuration.askForSettingsApi()).isTrue
        Assertions.assertThat(configuration.failOnSettingsApiSuspended()).isFalse
        Assertions.assertThat(configuration.ignoreLastKnowLocation()).isFalse
        Assertions.assertThat(configuration.googlePlayServicesWaitPeriod()).isEqualTo((20 * SECOND).toLong())
    }

    @Test
    fun setWaitPeriodShouldThrowExceptionWhenGooglePlayServicesWaitPeriodIsSet() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"))
        GooglePlayServicesConfiguration.Builder().setWaitPeriod(-1)
    }

    @Test
    fun clonesShouldShareSameInstances() {
        val configuration = GooglePlayServicesConfiguration.Builder().build()
        val firstClone = configuration.newBuilder().build()
        val secondClone = configuration.newBuilder().build()
        Assertions.assertThat(firstClone.locationRequest())
                .isEqualTo(secondClone.locationRequest())
                .isEqualTo(createDefaultLocationRequest())
        Assertions.assertThat(firstClone.askForGooglePlayServices())
                .isEqualTo(secondClone.askForGooglePlayServices())
                .isFalse
        Assertions.assertThat(firstClone.askForSettingsApi())
                .isEqualTo(secondClone.askForSettingsApi())
                .isTrue
        Assertions.assertThat(firstClone.failOnSettingsApiSuspended())
                .isEqualTo(secondClone.failOnSettingsApiSuspended())
                .isFalse
        Assertions.assertThat(firstClone.ignoreLastKnowLocation())
                .isEqualTo(secondClone.ignoreLastKnowLocation())
                .isFalse
        Assertions.assertThat(firstClone.googlePlayServicesWaitPeriod())
                .isEqualTo(secondClone.googlePlayServicesWaitPeriod())
                .isEqualTo((20 * SECOND).toLong())
    }

    private fun createDefaultLocationRequest(): LocationRequest {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval((5 * MINUTE).toLong())
                .setFastestInterval(MINUTE.toLong())
    }

    companion object {
        private const val SECOND = 1000
        private const val MINUTE = 60 * SECOND
    }
}