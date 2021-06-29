package bazaar.tech.library.location.locationmanager.providers.permissionprovider

import android.app.Activity
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.constants.RequestCode.RUNTIME_PERMISSION
import bazaar.tech.library.location.listener.PermissionListener
import bazaar.tech.library.location.locationmanager.fakes.MockDialogProvider
import bazaar.tech.library.location.providers.permissionprovider.DefaultPermissionProvider
import bazaar.tech.library.location.providers.permissionprovider.PermissionCompatSource
import bazaar.tech.library.location.view.ContextProcessor
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class DefaultPermissionProviderTest {
    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    @Mock
    lateinit var fragment: Fragment

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var contextProcessor: ContextProcessor

    @Mock
    lateinit var permissionListener: PermissionListener

    @Mock
    lateinit var permissionCompatSource: PermissionCompatSource
    private lateinit var defaultPermissionProvider: DefaultPermissionProvider
    private lateinit var mockDialogProvider: MockDialogProvider
    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockDialogProvider = MockDialogProvider("")
        defaultPermissionProvider = DefaultPermissionProvider(REQUIRED_PERMISSIONS, mockDialogProvider)
        defaultPermissionProvider!!.setContextProcessor(contextProcessor!!)
        defaultPermissionProvider!!.setPermissionListener(permissionListener!!)
        defaultPermissionProvider!!.permissionCompatSource = permissionCompatSource!!
    }

    @Test
    fun executePermissionsRequestShouldNotifyDeniedWhenThereIsNoActivityOrFragment() {
        defaultPermissionProvider!!.executePermissionsRequest()
        Mockito.verify(permissionListener).onPermissionsDenied()
    }



    @Test
    fun executePermissionsRequestShouldCallRequestPermissionsOnActivityIfThereIsNoFragment() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
        defaultPermissionProvider!!.executePermissionsRequest()
        verifyRequestPermissionOnActivity()
    }

    @Test
    fun checkRationaleForPermissionShouldReturnFalseIfThereIsNoActivityOrFragment() {
        Assertions.assertThat(defaultPermissionProvider!!.checkRationaleForPermission(SINGLE_PERMISSION)).isFalse
    }

    @Test
    fun checkRationaleForPermissionShouldCheckOnActivityIfThereIsNoFragment() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
        defaultPermissionProvider!!.checkRationaleForPermission(SINGLE_PERMISSION)
        Mockito.verify(permissionCompatSource).shouldShowRequestPermissionRationale(activity, SINGLE_PERMISSION)
    }

    @Test
    fun shouldShowRequestPermissionRationaleShouldReturnTrueWhenAnyIsTrue() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
        Mockito.`when`(permissionCompatSource!!.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSIONS[0]))
                .thenReturn(true)
        Mockito.`when`(permissionCompatSource!!.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSIONS[1]))
                .thenReturn(false)
        Mockito.`when`(permissionCompatSource!!.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSIONS[2]))
                .thenReturn(false)
        Assertions.assertThat(defaultPermissionProvider!!.shouldShowRequestPermissionRationale()).isTrue
    }



    @Test
    fun shouldShowRequestPermissionRationaleShouldReturnFalseWhenThereIsNoDialogProvider() {
        defaultPermissionProvider = DefaultPermissionProvider(REQUIRED_PERMISSIONS, null)
        defaultPermissionProvider!!.setContextProcessor(contextProcessor!!)
        defaultPermissionProvider!!.setPermissionListener(permissionListener!!)
        defaultPermissionProvider!!.permissionCompatSource = permissionCompatSource!!
        makeShouldShowRequestPermissionRationaleTrue()
        Assertions.assertThat(defaultPermissionProvider!!.shouldShowRequestPermissionRationale()).isFalse
    }

    @Test
    fun requestPermissionsShouldReturnFalseWhenThereIsNoActivity() {
        Assertions.assertThat(defaultPermissionProvider!!.requestPermissions()).isFalse
    }

    @Test
    fun requestPermissionsShouldRequestWhenShouldShowRequestPermissionRationaleIsFalse() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
        defaultPermissionProvider!!.requestPermissions()
        verifyRequestPermissionOnActivity()
    }

    @Test
    fun requestPermissionsShouldShowRationaleIfRequired() {
        makeShouldShowRequestPermissionRationaleTrue()
        defaultPermissionProvider!!.requestPermissions()
        Mockito.verify(mockDialogProvider!!.getDialog(activity!!)).show()
    }

    @Test
    fun onPositiveButtonClickShouldRequestPermission() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
        defaultPermissionProvider!!.onPositiveButtonClick()
        verifyRequestPermissionOnActivity()
    }

    @Test
    fun onNegativeButtonClickShouldNotifyPermissionDenied() {
        defaultPermissionProvider!!.onNegativeButtonClick()
        Mockito.verify(permissionListener).onPermissionsDenied()
    }

    @Test
    fun onRequestPermissionsResultShouldNotifyDeniedIfAny() {
        defaultPermissionProvider!!.onRequestPermissionsResult(RequestCode.RUNTIME_PERMISSION,
                REQUIRED_PERMISSIONS, intArrayOf(GRANTED, GRANTED, DENIED))
        Mockito.verify(permissionListener).onPermissionsDenied()
    }

    @Test
    fun onRequestPermissionsResultShouldNotifyGrantedIfAll() {
        defaultPermissionProvider!!.onRequestPermissionsResult(RequestCode.RUNTIME_PERMISSION,
                REQUIRED_PERMISSIONS, intArrayOf(GRANTED, GRANTED, GRANTED))
        Mockito.verify(permissionListener).onPermissionsGranted()
    }

    private fun makeShouldShowRequestPermissionRationaleTrue() {
        Mockito.`when`(contextProcessor!!.activity).thenReturn(activity)
        Mockito.`when`(permissionCompatSource!!.shouldShowRequestPermissionRationale(activity, REQUIRED_PERMISSIONS[0]))
                .thenReturn(true)
    }

    private fun verifyRequestPermissionOnActivity() {
        Mockito.verify(permissionCompatSource)
                .requestPermissions(activity, REQUIRED_PERMISSIONS, RUNTIME_PERMISSION)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf("really_important_permission",
                "even_more_important", "super_important_one")
        private val SINGLE_PERMISSION = REQUIRED_PERMISSIONS[0]
        private const val GRANTED = PackageManager.PERMISSION_GRANTED
        private const val DENIED = PackageManager.PERMISSION_DENIED
    }
}