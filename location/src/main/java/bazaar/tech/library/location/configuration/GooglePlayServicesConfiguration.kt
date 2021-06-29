package bazaar.tech.library.location.configuration

import com.google.android.gms.location.LocationRequest
import bazaar.tech.library.location.providers.locationprovider.DefaultLocationProvider
import bazaar.tech.library.location.providers.locationprovider.GooglePlayServicesLocationProvider

class GooglePlayServicesConfiguration private constructor(builder: Builder) {
    private var locationRequest: LocationRequest
    private val fallbackToDefault: Boolean
    private val askForGooglePlayServices: Boolean
    private val askForSettingsApi: Boolean
    private val failOnSettingsApiSuspended: Boolean
    private val ignoreLastKnowLocation: Boolean
    private val googlePlayServicesWaitPeriod: Long
    fun newBuilder(): Builder {
        return Builder()
                .locationRequest(locationRequest)
                .fallbackToDefault(fallbackToDefault)
                .askForGooglePlayServices(askForGooglePlayServices)
                .askForSettingsApi(askForSettingsApi)
                .failOnSettingsApiSuspended(failOnSettingsApiSuspended)
                .ignoreLastKnowLocation(ignoreLastKnowLocation)
                .setWaitPeriod(googlePlayServicesWaitPeriod)
    }

    // region Getters
    fun locationRequest(): LocationRequest {
        return locationRequest
    }

    fun fallbackToDefault(): Boolean {
        return fallbackToDefault
    }

    fun askForGooglePlayServices(): Boolean {
        return askForGooglePlayServices
    }

    fun askForSettingsApi(): Boolean {
        return askForSettingsApi
    }

    fun failOnSettingsApiSuspended(): Boolean {
        return failOnSettingsApiSuspended
    }

    fun ignoreLastKnowLocation(): Boolean {
        return ignoreLastKnowLocation
    }

    fun googlePlayServicesWaitPeriod(): Long {
        return googlePlayServicesWaitPeriod
    }

    // endregion
    class Builder {
        var locationRequest = Defaults.createDefaultLocationRequest()
        var fallbackToDefault = Defaults.FALLBACK_TO_DEFAULT
        var askForGooglePlayServices = Defaults.ASK_FOR_GP_SERVICES
        var askForSettingsApi = Defaults.ASK_FOR_SETTINGS_API
        var failOnSettingsApiSuspended = Defaults.FAIL_ON_SETTINGS_API_SUSPENDED
        var ignoreLastKnowLocation = Defaults.IGNORE_LAST_KNOW_LOCATION
        var googlePlayServicesWaitPeriod = Defaults.WAIT_PERIOD.toLong()

        /**
         * LocationRequest object that you specified to use while getting location from Google Play Services
         * Default is [Defaults.createDefaultLocationRequest]
         */
        fun locationRequest(locationRequest: LocationRequest): Builder {
            this.locationRequest = locationRequest
            return this
        }

        /**
         * In case of getting location from [GooglePlayServicesLocationProvider] fails,
         * library will fallback to [DefaultLocationProvider] as a default behaviour.
         * If you set this to false, then library will notify fail as soon as GooglePlayServicesLocationProvider fails.
         */
        fun fallbackToDefault(fallbackToDefault: Boolean): Builder {
            this.fallbackToDefault = fallbackToDefault
            return this
        }

        /**
         * Set true to ask user handle when there is some resolvable error
         * on connection GooglePlayServices, if you don't want to bother user
         * to configure Google Play Services to receive location then set this as false.
         *
         * Default is False.
         */
        fun askForGooglePlayServices(askForGooglePlayServices: Boolean): Builder {
            this.askForGooglePlayServices = askForGooglePlayServices
            return this
        }

        /**
         * While trying to get location via GooglePlayServices LocationApi,
         * manager will check whether GPS, Wifi and Cell networks are available or not.
         * Then if this flag is on it will ask user to turn them on, again, via GooglePlayServices
         * by displaying a system dialog if not it will directly try to receive location
         * -which probably not going to return any values.
         *
         * Default is True.
         */
        fun askForSettingsApi(askForSettingsApi: Boolean): Builder {
            this.askForSettingsApi = askForSettingsApi
            return this
        }

        /**
         * This flag will be checked when it is not possible to display user a settingsApi dialog
         * to switch necessary providers on, or when there is an error displaying the dialog.
         * If the flag is on, then manager will setDialogListener listener as location failed,
         * otherwise it will try to get location anyway -which probably not gonna happen.
         *
         * Default is False. -Because after GooglePlayServices Provider it might switch
         * to default providers, if we fail here then those provider will never trigger.
         */
        fun failOnSettingsApiSuspended(failOnSettingsApiSuspended: Boolean): Builder {
            this.failOnSettingsApiSuspended = failOnSettingsApiSuspended
            return this
        }

        /**
         * GooglePlayServices Api returns the best most recent location currently available. It is highly recommended to
         * use this functionality unless your requirements are really specific and precise.
         *
         * Default is False. So GooglePlayServices Api will return immediately if there is location already.
         *
         * https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi.html
         * #getLastLocation(com.google.android.gms.common.api.GoogleApiClient)
         */
        fun ignoreLastKnowLocation(ignore: Boolean): Builder {
            ignoreLastKnowLocation = ignore
            return this
        }

        /**
         * Indicates waiting time period for GooglePlayServices before switching to next possible provider.
         *
         * Default values are [Defaults.WAIT_PERIOD]
         */
        fun setWaitPeriod(milliseconds: Long): Builder {
            require(milliseconds >= 0) { "waitPeriod cannot be set to negative value." }
            googlePlayServicesWaitPeriod = milliseconds
            return this
        }

        fun build(): GooglePlayServicesConfiguration {
            return GooglePlayServicesConfiguration(this)
        }
    }

    init {
        locationRequest = builder.locationRequest
        fallbackToDefault = builder.fallbackToDefault
        askForGooglePlayServices = builder.askForGooglePlayServices
        askForSettingsApi = builder.askForSettingsApi
        failOnSettingsApiSuspended = builder.failOnSettingsApiSuspended
        ignoreLastKnowLocation = builder.ignoreLastKnowLocation
        googlePlayServicesWaitPeriod = builder.googlePlayServicesWaitPeriod
    }
}