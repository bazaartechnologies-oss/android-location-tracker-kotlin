package bazaar.tech.library.location.helper

object StringUtils {
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.length == 0
    }

    fun isNotEmpty(str: CharSequence?): Boolean {
        return str != null && str.length > 0
    }
}