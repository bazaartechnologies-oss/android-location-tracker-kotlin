package bazaar.tech.library.location.helper

import bazaar.tech.library.location.helper.logging.DefaultLogger
import bazaar.tech.library.location.helper.logging.Logger

object LogUtils {
    private var isEnabled = false
    private var activeLogger: Logger = DefaultLogger()
    @kotlin.jvm.JvmStatic
    fun enable(isEnabled: Boolean) {
        LogUtils.isEnabled = isEnabled
    }

    @kotlin.jvm.JvmStatic
    fun setLogger(logger: Logger) {
        activeLogger = logger
    }

    @kotlin.jvm.JvmStatic
    fun logD(message: String?) {
        if (isEnabled) activeLogger.logD(className, message)
    }

    @kotlin.jvm.JvmStatic
    fun logE(message: String?) {
        if (isEnabled) activeLogger.logE(className, message)
    }

    @kotlin.jvm.JvmStatic
    fun logI(message: String?) {
        if (isEnabled) activeLogger.logI(className, message)
    }

    @kotlin.jvm.JvmStatic
    fun logV(message: String?) {
        if (isEnabled) activeLogger.logV(className, message)
    }

    @kotlin.jvm.JvmStatic
    fun logW(message: String?) {
        if (isEnabled) activeLogger.logW(className, message)
    }

    private val className: String
        get() {
            val trace = Thread.currentThread().stackTrace
            val relevantTrace = trace[4]
            val className = relevantTrace.className
            val lastIndex = className.lastIndexOf('.')
            return className.substring(lastIndex + 1)
        }
}