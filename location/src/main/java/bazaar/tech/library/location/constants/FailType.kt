package bazaar.tech.library.location.constants

import androidx.annotation.IntDef

@IntDef(FailType.UNKNOWN, FailType.TIMEOUT, FailType.PERMISSION_DENIED, FailType.NETWORK_NOT_AVAILABLE, FailType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE, FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG, FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED, FailType.VIEW_DETACHED, FailType.VIEW_NOT_REQUIRED_TYPE)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class FailType {
    companion object {
        const val UNKNOWN = -1
        const val TIMEOUT = 1
        const val PERMISSION_DENIED = 2
        const val NETWORK_NOT_AVAILABLE = 3
        const val GOOGLE_PLAY_SERVICES_NOT_AVAILABLE = 4
        const val GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG = 6
        const val GOOGLE_PLAY_SERVICES_SETTINGS_DENIED = 7
        const val VIEW_DETACHED = 8
        const val VIEW_NOT_REQUIRED_TYPE = 9
        const val DEFAULT_CONFIGURATION_NOT_FOUND = 10
        const val GOOGLE_PLAY_CONFIGURATION_NOT_FOUND = 11
    }
}