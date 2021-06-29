package bazaar.tech.library.location.configuration

object Configurations {
    /**
     * Returns a LocationConfiguration that will never ask user anything and will try to use whatever possible options
     * that application has to obtain location. If there is no sufficient permission, provider, etc... then
     * LocationManager will call [LocationListener.onLocationFailed] silently
     *
     * # Best use case of this configuration is within Service implementations
     */
    /**
     * Returns a LocationConfiguration that keeps tracking,
     * see also [Configurations.silentConfiguration]
     */
    @kotlin.jvm.JvmStatic
    fun silentConfiguration(keepTracking: Boolean = true): LocationConfiguration {
        return LocationConfiguration.Builder()
                .keepTracking(keepTracking)
                .useGooglePlayServices(GooglePlayServicesConfiguration.Builder().askForSettingsApi(false).build())
                .useDefaultProviders(DefaultProviderConfiguration.Builder().build())
                .build()
    }

    /**
     * Returns a LocationConfiguration which tights to default definitions with given messages. Since this method is
     * basically created in order to be used in Activities, User needs to be asked for permission and enabling gps.
     */
    @kotlin.jvm.JvmStatic
    fun defaultConfiguration(rationalMessage: String, gpsMessage: String): LocationConfiguration {
        return LocationConfiguration.Builder()
                .askForPermission(PermissionConfiguration.Builder().rationaleMessage(rationalMessage).build())
                .useGooglePlayServices(GooglePlayServicesConfiguration.Builder().build())
                .useDefaultProviders(DefaultProviderConfiguration.Builder().gpsMessage(gpsMessage).build())
                .build()
    }
}