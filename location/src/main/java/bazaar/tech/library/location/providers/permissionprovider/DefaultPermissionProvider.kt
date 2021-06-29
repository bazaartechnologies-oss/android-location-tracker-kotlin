package bazaar.tech.library.location.providers.permissionprovider

import android.content.pm.PackageManager
import bazaar.tech.library.location.constants.RequestCode
import bazaar.tech.library.location.helper.LogUtils
import bazaar.tech.library.location.listener.DialogListener
import bazaar.tech.library.location.providers.dialogprovider.DialogProvider

class DefaultPermissionProvider(requiredPermissions: Array<String>, dialogProvider: DialogProvider?) : PermissionProvider(requiredPermissions, dialogProvider), DialogListener {
    // For test purposes
    var permissionCompatSource: PermissionCompatSource = PermissionCompatSource()

    override fun requestPermissions(): Boolean {
        if (activity == null) {
            LogUtils.logI("Cannot ask for permissions, "
                    + "because DefaultPermissionProvider doesn't contain an Activity instance.")
            return false
        }
        if (shouldShowRequestPermissionRationale()) {
            dialogProvider?.dialogListener = this
            dialogProvider?.getDialog(activity!!)?.show()
        } else {
            executePermissionsRequest()
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RequestCode.RUNTIME_PERMISSION) {

            // Check if any of required permissions are denied.
            var isDenied = false
            var i = 0
            val size = permissions.size
            while (i < size) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isDenied = true
                }
                i++
            }
            if (isDenied) {
                LogUtils.logI("User denied some of required permissions, task will be aborted!")
                permissionListener?.onPermissionsDenied()
            } else {
                LogUtils.logI("We got all required permission!")
                permissionListener?.onPermissionsGranted()
            }
        }
    }

    override fun onPositiveButtonClick() {
        executePermissionsRequest()
    }

    override fun onNegativeButtonClick() {
        LogUtils.logI("User didn't even let us to ask for permission!")
        permissionListener?.onPermissionsDenied()
    }

    fun shouldShowRequestPermissionRationale(): Boolean {
        var shouldShowRationale = false
        for (permission in requiredPermissions) {
            shouldShowRationale = shouldShowRationale || checkRationaleForPermission(permission)
        }
        LogUtils.logI("Should show rationale dialog for required permissions: $shouldShowRationale")
        return shouldShowRationale && activity != null && dialogProvider != null
    }

    fun checkRationaleForPermission(permission: String): Boolean {
        return when {
            fragment != null -> {
                permissionCompatSource.shouldShowRequestPermissionRationale(fragment!!, permission)
            }
            activity != null -> {
                permissionCompatSource.shouldShowRequestPermissionRationale(activity!!, permission)
            }
            else -> {
                false
            }
        }
    }

    fun executePermissionsRequest() {
        LogUtils.logI("Asking for Runtime Permissions...")
        when {
            fragment != null -> {
                permissionCompatSource.requestPermissions(fragment!!,
                        requiredPermissions, RequestCode.RUNTIME_PERMISSION)
            }
            activity != null -> {
                permissionCompatSource.requestPermissions(activity!!,
                        requiredPermissions, RequestCode.RUNTIME_PERMISSION)
            }
            else -> {
                LogUtils.logE("Something went wrong requesting for permissions.")
                permissionListener?.onPermissionsDenied()
            }
        }
    }
}