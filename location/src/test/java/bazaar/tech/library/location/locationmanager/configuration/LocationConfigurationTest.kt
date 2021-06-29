package bazaar.tech.library.location.locationmanager.configuration

import bazaar.tech.library.location.configuration.DefaultProviderConfiguration
import bazaar.tech.library.location.configuration.GooglePlayServicesConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.configuration.PermissionConfiguration
import bazaar.tech.library.location.providers.permissionprovider.DefaultPermissionProvider
import bazaar.tech.library.location.providers.permissionprovider.StubPermissionProvider
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class LocationConfigurationTest {
    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    @Test
    fun checkDefaultValues() {
        val configuration = configuration
        Assertions.assertThat(configuration.keepTracking()).isFalse
    }

    @Test
    fun whenNoPermissionConfigurationIsSetDefaultConfigurationShouldContainStubProvider() {
        val configuration = configuration
        Assertions.assertThat(configuration.permissionConfiguration()).isNotNull
        Assertions.assertThat(configuration.permissionConfiguration().permissionProvider())
                .isNotNull
                .isExactlyInstanceOf(StubPermissionProvider::class.java)
    }

    @Test
    fun clonesShouldShareSameInstances() {
        val configuration = configuration
        val firstClone = configuration.newBuilder().build()
        val secondClone = configuration.newBuilder().build()
        Assertions.assertThat(firstClone.keepTracking())
                .isEqualTo(secondClone.keepTracking())
                .isFalse
        Assertions.assertThat(firstClone.permissionConfiguration())
                .isEqualTo(secondClone.permissionConfiguration())
                .isNotNull
        Assertions.assertThat(firstClone.defaultProviderConfiguration())
                .isEqualTo(secondClone.defaultProviderConfiguration())
                .isNotNull
        Assertions.assertThat(firstClone.googlePlayServicesConfiguration())
                .isEqualTo(secondClone.googlePlayServicesConfiguration())
                .isNotNull
    }

    @Test
    fun clonedConfigurationIsIndependent() {
        val configuration = configuration
        val clone = configuration.newBuilder()
                .askForPermission(PermissionConfiguration.Builder().build())
                .build()
        Assertions.assertThat(configuration.permissionConfiguration())
                .isNotEqualTo(clone.permissionConfiguration())
        Assertions.assertThat(configuration.permissionConfiguration().permissionProvider())
                .isNotNull
                .isExactlyInstanceOf(StubPermissionProvider::class.java)
        Assertions.assertThat(clone.permissionConfiguration().permissionProvider())
                .isNotNull
                .isExactlyInstanceOf(DefaultPermissionProvider::class.java)
    }

    private val configuration: LocationConfiguration
        private get() = LocationConfiguration.Builder()
                .useDefaultProviders(DefaultProviderConfiguration.Builder().build())
                .useGooglePlayServices(GooglePlayServicesConfiguration.Builder().build())
                .build()
}