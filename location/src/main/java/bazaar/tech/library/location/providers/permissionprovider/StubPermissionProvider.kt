package bazaar.tech.library.location.providers.permissionprovider

import bazaar.tech.library.location.configuration.Defaults

class StubPermissionProvider : PermissionProvider(Defaults.LOCATION_PERMISSIONS, null) {
    override fun requestPermissions(): Boolean {
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

    }
}