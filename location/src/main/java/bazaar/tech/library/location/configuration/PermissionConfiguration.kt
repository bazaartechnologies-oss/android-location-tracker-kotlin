package bazaar.tech.library.location.configuration

import bazaar.tech.library.location.helper.StringUtils
import bazaar.tech.library.location.providers.dialogprovider.DialogProvider
import bazaar.tech.library.location.providers.dialogprovider.SimpleMessageDialogProvider
import bazaar.tech.library.location.providers.permissionprovider.DefaultPermissionProvider
import bazaar.tech.library.location.providers.permissionprovider.PermissionProvider

class PermissionConfiguration private constructor(val permissionProvider: PermissionProvider) {
    fun permissionProvider() = permissionProvider

    class Builder {
        private var rationaleMessage: String = Defaults.EMPTY_STRING
        private var requiredPermissions = Defaults.LOCATION_PERMISSIONS
        private var rationaleDialogProvider: DialogProvider? = null
        var permissionProvider: PermissionProvider? = null

        /**
         * Indicates what to display when user needs to see a rational dialog for RuntimePermission.
         * There is no default value, so if you do not set this, user will not see any rationale dialog.
         *
         * And if you set [PermissionConfiguration.Builder.rationaleDialogProvider] then this
         * field will be ignored. Please make sure you handled in your custom dialogProvider implementation.
         */
        fun rationaleMessage(rationaleMessage: String): Builder {
            this.rationaleMessage = rationaleMessage
            return this
        }

        /**
         * If you need to ask any other permissions beside [Defaults.LOCATION_PERMISSIONS]
         * or you may not need both of those permissions, you can change permissions
         * by calling this method with new permissions' array.
         */
        fun requiredPermissions(permissions: Array<String>): Builder {
            require(permissions.isNotEmpty()) { "requiredPermissions cannot be empty." }
            requiredPermissions = permissions
            return this
        }

        /**
         * If you need to display a custom dialog to display rationale to user, you can provide your own
         * implementation of [DialogProvider] and manager will use that implementation to display the dialog.
         * Important, if you set your own implementation, please make sure to handle rationaleMessage as well.
         * Because [PermissionConfiguration.Builder.rationaleMessage] will be ignored in that case.
         *
         * If you don't specify any dialogProvider implementation [SimpleMessageDialogProvider] will be used with
         * given [PermissionConfiguration.Builder.rationaleMessage]
         */
        fun rationaleDialogProvider(dialogProvider: DialogProvider): Builder {
            rationaleDialogProvider = dialogProvider
            return this
        }

        /**
         * If you already have a mechanism to handle runtime permissions, you can provide your own implementation of
         * [PermissionProvider] and manager will use that implementation to ask required permissions.
         * Important, if you set your own implementation, please make sure to handle dialogProvider as well.
         * Because [PermissionConfiguration.Builder.rationaleDialogProvider] will be ignored in that case.
         *
         * If you don't specify any permissionProvider implementation [DefaultPermissionProvider] will be used
         * with given [PermissionConfiguration.Builder.rationaleDialogProvider]
         */
        fun permissionProvider(permissionProvider: PermissionProvider): Builder {
            this.permissionProvider = permissionProvider
            return this
        }

        fun build(): PermissionConfiguration {
            if (rationaleDialogProvider == null && StringUtils.isNotEmpty(rationaleMessage)) {
                rationaleDialogProvider = SimpleMessageDialogProvider(rationaleMessage)
            }
            if (permissionProvider == null) {
                permissionProvider = DefaultPermissionProvider(requiredPermissions, rationaleDialogProvider)
            }
            return PermissionConfiguration(permissionProvider!!)
        }
    }

}