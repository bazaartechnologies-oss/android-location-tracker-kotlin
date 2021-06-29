package bazaar.tech.library.location.listener

interface PermissionListener {
    /**
     * Notify when user is granted all required permissions
     */
    fun onPermissionsGranted()

    /**
     * Notify when user is denied any one of required permissions
     */
    fun onPermissionsDenied()
}