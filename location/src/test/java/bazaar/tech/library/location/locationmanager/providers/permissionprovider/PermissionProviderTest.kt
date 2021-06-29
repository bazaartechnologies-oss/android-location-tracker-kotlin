package bazaar.tech.library.location.locationmanager.providers.permissionprovider

import android.content.Context
import bazaar.tech.library.location.locationmanager.fakes.FakePermissionProvider
import bazaar.tech.library.location.view.ContextProcessor
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class PermissionProviderTest {
    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    @Mock
    var contextProcessor: ContextProcessor? = null

    @Mock
    var context: Context? = null
    private var permissionProvider: FakePermissionProvider? = null
    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        permissionProvider = FakePermissionProvider(REQUIRED_PERMISSIONS, null)
        permissionProvider!!.setContextProcessor(contextProcessor!!)
    }

    @Test
    fun creatingInstanceWithNoRequiredPermissionShouldThrowException() {
        expectedException.expect(IllegalStateException::class.java)
        permissionProvider = FakePermissionProvider(arrayOf(), null)
    }

    @Test
    fun whenThereIsNoContextHasPermissionShouldReturnFalse() {
        Assertions.assertThat(permissionProvider!!.hasPermission()).isFalse
    }

    @Test
    fun whenThereIsContextHasPermissionShouldReturnTrue() {
        Mockito.`when`(contextProcessor!!.context).thenReturn(context)
        permissionProvider!!.grantPermission(true)
        Assertions.assertThat(permissionProvider!!.hasPermission()).isTrue
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf<String>("really_important_permission")
    }
}