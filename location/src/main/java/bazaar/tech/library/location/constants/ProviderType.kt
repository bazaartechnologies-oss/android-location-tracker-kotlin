package bazaar.tech.library.location.constants

import androidx.annotation.IntDef

@IntDef(ProviderType.NONE, ProviderType.GOOGLE_PLAY_SERVICES, ProviderType.GPS, ProviderType.NETWORK, ProviderType.DEFAULT_PROVIDERS)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class ProviderType {
    companion object {
        const val NONE = 0
        const val GOOGLE_PLAY_SERVICES = 1
        const val GPS = 2
        const val NETWORK = 3
        const val DEFAULT_PROVIDERS = 4 // Covers both GPS and NETWORK
    }
}