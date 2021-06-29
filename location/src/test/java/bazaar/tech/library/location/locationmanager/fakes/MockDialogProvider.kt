package bazaar.tech.library.location.locationmanager.fakes

import android.app.Dialog
import android.content.Context
import bazaar.tech.library.location.providers.dialogprovider.DialogProvider
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MockDialogProvider(private val message: String) : DialogProvider() {
    @Mock
    var dialog: Dialog? = null
    fun message(): String {
        return message
    }

    override fun getDialog(context: Context): Dialog {
        return dialog!!
    }

    init {
        MockitoAnnotations.initMocks(this)
    }
}