package bazaar.tech.library.location.helper.logging

interface Logger {
    fun logD(className: String?, message: String?)
    fun logE(className: String?, message: String?)
    fun logI(className: String?, message: String?)
    fun logV(className: String?, message: String?)
    fun logW(className: String?, message: String?)
}