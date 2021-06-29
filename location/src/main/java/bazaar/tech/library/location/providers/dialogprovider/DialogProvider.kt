package bazaar.tech.library.location.providers.dialogprovider

import android.app.Dialog
import android.content.Context
import bazaar.tech.library.location.listener.DialogListener
import bazaar.tech.library.location.providers.permissionprovider.DefaultPermissionProvider
import java.lang.ref.WeakReference

abstract class DialogProvider {
    private var weakDialogListener: WeakReference<DialogListener>? = null

    /**
     * Create a dialog object on given context
     *
     * @param context in which the dialog should run
     * @return dialog object to display
     */
    abstract fun getDialog(context: Context): Dialog

    /**
     * Sets a [DialogListener] to provide pre-defined actions to the component which uses this dialog
     *
     * This method will be called by [DefaultPermissionProvider] internally, if it is in use.
     *
     * @param dialogListener will be used to notify on specific actions
     */
    var dialogListener: DialogListener?
        get() = weakDialogListener?.get()
        set (dialogListener) {
            weakDialogListener = WeakReference(dialogListener)
        }
}