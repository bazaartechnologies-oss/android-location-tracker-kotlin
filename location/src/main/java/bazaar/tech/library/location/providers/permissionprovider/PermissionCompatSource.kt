package bazaar.tech.library.location.providers.permissionprovider

import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class PermissionCompatSource {
    fun shouldShowRequestPermissionRationale(fragment: Fragment, permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }

    fun requestPermissions(fragment: Fragment, requiredPermissions: Array<String>, requestCode: Int) {
        fragment.requestPermissions(requiredPermissions, requestCode)
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun requestPermissions(activity: Activity, requiredPermissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, requestCode)
    }
}