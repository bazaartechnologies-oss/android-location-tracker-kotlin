package bazaar.tech.library.location.locationmanager.configuration

import bazaar.tech.library.location.configuration.Configurations.defaultConfiguration
import bazaar.tech.library.location.configuration.Configurations.silentConfiguration
import bazaar.tech.library.location.providers.dialogprovider.SimpleMessageDialogProvider
import bazaar.tech.library.location.providers.permissionprovider.DefaultPermissionProvider
import bazaar.tech.library.location.providers.permissionprovider.StubPermissionProvider
import org.assertj.core.api.Assertions
import org.junit.Test

class ConfigurationsTest {
    @Test
    fun silentConfigurationWithoutParameterShouldKeepTracking() {
        Assertions.assertThat(silentConfiguration().keepTracking()).isTrue
    }

    @Test
    fun silentConfigurationCheckDefaultValues() {
        val silentConfiguration = silentConfiguration(false)
        Assertions.assertThat(silentConfiguration.keepTracking()).isFalse
        Assertions.assertThat(silentConfiguration.permissionConfiguration()).isNotNull
        Assertions.assertThat(silentConfiguration.permissionConfiguration().permissionProvider())
                .isNotNull
                .isExactlyInstanceOf(StubPermissionProvider::class.java)
        Assertions.assertThat(silentConfiguration.googlePlayServicesConfiguration()).isNotNull
        Assertions.assertThat(silentConfiguration.googlePlayServicesConfiguration().askForSettingsApi()).isFalse
        Assertions.assertThat(silentConfiguration.defaultProviderConfiguration()).isNotNull
    }

    @Test
    fun defaultConfigurationCheckDefaultValues() {
        val defaultConfiguration = defaultConfiguration("rationale", "gps")
        Assertions.assertThat(defaultConfiguration.keepTracking()).isFalse
        Assertions.assertThat(defaultConfiguration.permissionConfiguration()).isNotNull
        Assertions.assertThat(defaultConfiguration.permissionConfiguration().permissionProvider())
                .isNotNull
                .isExactlyInstanceOf(DefaultPermissionProvider::class.java)
        Assertions.assertThat(defaultConfiguration.permissionConfiguration().permissionProvider().dialogProvider)
                .isNotNull
                .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        Assertions.assertThat((defaultConfiguration.permissionConfiguration()
                .permissionProvider().dialogProvider as SimpleMessageDialogProvider?)!!.message()).isEqualTo("rationale")
        Assertions.assertThat(defaultConfiguration.googlePlayServicesConfiguration()).isNotNull
        Assertions.assertThat(defaultConfiguration.defaultProviderConfiguration()).isNotNull
        Assertions.assertThat(defaultConfiguration.defaultProviderConfiguration().askForEnableGPS()).isTrue
        Assertions.assertThat(defaultConfiguration.defaultProviderConfiguration().gpsDialogProvider())
                .isNotNull
                .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        Assertions.assertThat((defaultConfiguration.defaultProviderConfiguration()
                .gpsDialogProvider() as SimpleMessageDialogProvider?)!!.message()).isEqualTo("gps")
    }
}