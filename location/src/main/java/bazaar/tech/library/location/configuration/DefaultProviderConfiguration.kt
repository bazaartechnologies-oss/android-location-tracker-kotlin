package bazaar.tech.library.location.configuration

import bazaar.tech.library.location.constants.ProviderType
import bazaar.tech.library.location.helper.StringUtils
import bazaar.tech.library.location.providers.dialogprovider.DialogProvider
import bazaar.tech.library.location.providers.dialogprovider.SimpleMessageDialogProvider

class DefaultProviderConfiguration private constructor(builder: Builder) {
    private val requiredTimeInterval: Long
    private val requiredDistanceInterval: Long
    private val acceptableAccuracy: Float
    private val acceptableTimePeriod: Long
    private val gpsWaitPeriod: Long
    private val networkWaitPeriod: Long
    private val gpsDialogProvider: DialogProvider?
    fun newBuilder(): Builder {
        return Builder()
                .requiredTimeInterval(requiredTimeInterval)
                .requiredDistanceInterval(requiredDistanceInterval)
                .acceptableAccuracy(acceptableAccuracy)
                .acceptableTimePeriod(acceptableTimePeriod)
                .setWaitPeriod(ProviderType.GPS, gpsWaitPeriod)
                .setWaitPeriod(ProviderType.NETWORK, networkWaitPeriod)
                .gpsDialogProvider(gpsDialogProvider)
    }

    // region Getters
    fun requiredTimeInterval(): Long {
        return requiredTimeInterval
    }

    fun requiredDistanceInterval(): Long {
        return requiredDistanceInterval
    }

    fun acceptableAccuracy(): Float {
        return acceptableAccuracy
    }

    fun acceptableTimePeriod(): Long {
        return acceptableTimePeriod
    }

    fun askForEnableGPS(): Boolean {
        return gpsDialogProvider != null
    }

    fun gpsDialogProvider(): DialogProvider? {
        return gpsDialogProvider
    }

    fun gpsWaitPeriod(): Long {
        return gpsWaitPeriod
    }

    fun networkWaitPeriod(): Long {
        return networkWaitPeriod
    }

    // endregion
    class Builder {
        var requiredTimeInterval = Defaults.LOCATION_INTERVAL.toLong()
        var requiredDistanceInterval = Defaults.LOCATION_DISTANCE_INTERVAL.toLong()
        var acceptableAccuracy = Defaults.MIN_ACCURACY
        var acceptableTimePeriod = Defaults.TIME_PERIOD.toLong()
        var gpsWaitPeriod = Defaults.WAIT_PERIOD.toLong()
        var networkWaitPeriod = Defaults.WAIT_PERIOD.toLong()
        var gpsDialogProvider: DialogProvider? = null
        private var gpsMessage: String? = Defaults.EMPTY_STRING

        /**
         * TimeInterval will be used while getting location from default location providers
         * It will define in which period updates need to be delivered and will be used only when
         * [LocationConfiguration.keepTracking] is set to true.
         * Default is [Defaults.LOCATION_INTERVAL]
         */
        fun requiredTimeInterval(requiredTimeInterval: Long): Builder {
            require(requiredTimeInterval >= 0) { "requiredTimeInterval cannot be set to negative value." }
            this.requiredTimeInterval = requiredTimeInterval
            return this
        }

        /**
         * DistanceInterval will be used while getting location from default location providers
         * It will define in which distance changes that we should receive an update and will be used only when
         * [LocationConfiguration.keepTracking] is set to true.
         * Default is [Defaults.LOCATION_DISTANCE_INTERVAL]
         */
        fun requiredDistanceInterval(requiredDistanceInterval: Long): Builder {
            require(requiredDistanceInterval >= 0) { "requiredDistanceInterval cannot be set to negative value." }
            this.requiredDistanceInterval = requiredDistanceInterval
            return this
        }

        /**
         * Minimum Accuracy that you seek location to be
         * Default is [Defaults.MIN_ACCURACY]
         */
        fun acceptableAccuracy(acceptableAccuracy: Float): Builder {
            require(acceptableAccuracy >= 0) { "acceptableAccuracy cannot be set to negative value." }
            this.acceptableAccuracy = acceptableAccuracy
            return this
        }

        /**
         * Indicates time period that can be count as usable location,
         * this needs to be considered such as "last 5 minutes"
         * Default is [Defaults.TIME_PERIOD]
         */
        fun acceptableTimePeriod(acceptableTimePeriod: Long): Builder {
            require(acceptableTimePeriod >= 0) { "acceptableTimePeriod cannot be set to negative value." }
            this.acceptableTimePeriod = acceptableTimePeriod
            return this
        }

        /**
         * Indicates what to display to user while asking to turn GPS on.
         * If you do not set this, user will not be asked to enable GPS.
         */
        fun gpsMessage(gpsMessage: String?): Builder {
            this.gpsMessage = gpsMessage
            return this
        }

        /**
         * If you need to display a custom dialog to ask user to enable GPS, you can provide your own
         * implementation of [DialogProvider] and manager will use that implementation to display the dialog.
         * Important, if you set your own implementation, please make sure to handle gpsMessage as well.
         * Because [DefaultProviderConfiguration.Builder.gpsMessage] will be ignored in that case.
         *
         * If you don't specify any dialogProvider implementation [SimpleMessageDialogProvider] will be used with
         * given [DefaultProviderConfiguration.Builder.gpsMessage]
         */
        fun gpsDialogProvider(dialogProvider: DialogProvider?): Builder {
            gpsDialogProvider = dialogProvider
            return this
        }

        /**
         * Indicates waiting time period before switching to next possible provider.
         * Possible to set provider wait periods separately by passing providerType as one of the
         * [ProviderType] values.
         * Default values are [Defaults.WAIT_PERIOD]
         */
        fun setWaitPeriod(@ProviderType providerType: Int, milliseconds: Long): Builder {
            require(milliseconds >= 0) { "waitPeriod cannot be set to negative value." }
            when (providerType) {
                ProviderType.Companion.GOOGLE_PLAY_SERVICES -> {
                    throw IllegalStateException("GooglePlayServices waiting time period should be set on "
                            + "GooglePlayServicesConfiguration")
                }
                ProviderType.Companion.NETWORK -> {
                    networkWaitPeriod = milliseconds
                }
                ProviderType.Companion.GPS -> {
                    gpsWaitPeriod = milliseconds
                }
                ProviderType.Companion.DEFAULT_PROVIDERS -> {
                    gpsWaitPeriod = milliseconds
                    networkWaitPeriod = milliseconds
                }
                ProviderType.Companion.NONE -> {
                }
            }
            return this
        }

        fun build(): DefaultProviderConfiguration {
            if (gpsDialogProvider == null && StringUtils.isNotEmpty(gpsMessage)) {
                gpsDialogProvider = SimpleMessageDialogProvider(gpsMessage)
            }
            return DefaultProviderConfiguration(this)
        }
    }

    init {
        requiredTimeInterval = builder.requiredTimeInterval
        requiredDistanceInterval = builder.requiredDistanceInterval
        acceptableAccuracy = builder.acceptableAccuracy
        acceptableTimePeriod = builder.acceptableTimePeriod
        gpsWaitPeriod = builder.gpsWaitPeriod
        networkWaitPeriod = builder.networkWaitPeriod
        gpsDialogProvider = builder.gpsDialogProvider
    }
}