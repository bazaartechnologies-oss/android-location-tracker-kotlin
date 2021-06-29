package bazaar.tech.library.location.providers.dialogprovider

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class SimpleMessageDialogProvider(private val message: String?) : DialogProvider(), DialogInterface.OnClickListener {
    fun message(): String? {
        return message
    }

    override fun getDialog(context: Context): Dialog {
        return AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", this)
                .setNegativeButton("Cancel", this)
                .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                dialogListener?.onPositiveButtonClick()
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                dialogListener?.onNegativeButtonClick()
            }
        }
    }
}