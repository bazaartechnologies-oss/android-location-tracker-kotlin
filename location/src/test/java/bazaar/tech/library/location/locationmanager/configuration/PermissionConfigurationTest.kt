package bazaar.tech.library.location.locationmanager.configuration

import android.Manifest
import bazaar.tech.library.location.configuration.Defaults
import bazaar.tech.library.location.configuration.PermissionConfiguration
import bazaar.tech.library.location.locationmanager.fakes.MockDialogProvider
import bazaar.tech.library.location.providers.dialogprovider.SimpleMessageDialogProvider
import bazaar.tech.library.location.providers.permissionprovider.DefaultPermissionProvider
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.*

class PermissionConfigurationTest {
    @Rule
    @JvmField
    var expectedException = ExpectedException.none()
    @Test
    fun checkDefaultValues() {
        val configuration = PermissionConfiguration.Builder().build()
        Assertions.assertThat(configuration.permissionProvider())
                .isNotNull
                .isExactlyInstanceOf(DefaultPermissionProvider::class.java)
        Assertions.assertThat(configuration.permissionProvider().dialogProvider).isNull()
        Assertions.assertThat(configuration.permissionProvider().requiredPermissions)
                .isNotEmpty
                .isEqualTo(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION))
    }


    @Test
    fun requiredPermissionsShouldThrowExceptionWhenSetEmpty() {
        expectedException.expect(IllegalArgumentException::class.java)
        expectedException.expectMessage(CoreMatchers.startsWith("requiredPermissions"))
        PermissionConfiguration.Builder().requiredPermissions(arrayOf())
    }

    @Test
    fun requiredPermissionsShouldSetPermissionsWhenSetNotEmpty() {
        val permissionConfiguration = PermissionConfiguration.Builder().requiredPermissions(Defaults.LOCATION_PERMISSIONS).build()
        Assertions.assertThat(permissionConfiguration.permissionProvider().requiredPermissions).containsAll(Arrays.asList(*Defaults.LOCATION_PERMISSIONS))
    }

    @Test
    fun whenRationaleMessageIsNotEmptyDefaultDialogProviderShouldBeSimple() {
        val RATIONALE_MESSAGE = "some_text"
        val configuration = PermissionConfiguration.Builder()
                .rationaleMessage(RATIONALE_MESSAGE)
                .build()
        Assertions.assertThat(configuration.permissionProvider().dialogProvider)
                .isNotNull
                .isExactlyInstanceOf(SimpleMessageDialogProvider::class.java)
        Assertions.assertThat((configuration.permissionProvider().dialogProvider as SimpleMessageDialogProvider?)!!.message())
                .isEqualTo(RATIONALE_MESSAGE)
    }

    @Test
    fun whenDialogProviderIsSetMessageShouldBeIgnored() {
        val RATIONALE_MESSAGE = "some_text"
        val configuration = PermissionConfiguration.Builder()
                .rationaleDialogProvider(MockDialogProvider(RATIONALE_MESSAGE))
                .rationaleMessage("ignored_text")
                .build()
        Assertions.assertThat(configuration.permissionProvider().dialogProvider)
                .isNotNull
                .isExactlyInstanceOf(MockDialogProvider::class.java)
        Assertions.assertThat((configuration.permissionProvider().dialogProvider as MockDialogProvider?)!!.message())
                .isEqualTo(RATIONALE_MESSAGE)
    }
}