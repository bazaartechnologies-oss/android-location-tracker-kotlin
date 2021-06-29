package bazaar.tech.library.location.helper.logging

import android.util.Log

class DefaultLogger : Logger {
    override fun logD(className: String?, message: String?) {
        Log.d(className, message)
    }

    override fun logE(className: String?, message: String?) {
        Log.e(className, message)
    }

    override fun logI(className: String?, message: String?) {
        Log.i(className, message)
    }

    override fun logV(className: String?, message: String?) {
        Log.v(className, message)
    }

    override fun logW(className: String?, message: String?) {
        Log.w(className, message)
    }
}