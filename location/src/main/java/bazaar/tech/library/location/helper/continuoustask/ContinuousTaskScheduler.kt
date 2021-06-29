package bazaar.tech.library.location.helper.continuoustask

internal class ContinuousTaskScheduler(private val task: ContinuousTask) {
    private var requiredDelay = NONE
    private var initialTime = NONE
    private var remainingTime = NONE
    var isSet = false
        private set

    fun delayed(delay: Long) {
        requiredDelay = delay
        remainingTime = requiredDelay
        initialTime = task.currentTime
        set(delay)
    }

    fun onPause() {
        if (requiredDelay != NONE) {
            release()
            remainingTime = requiredDelay - (task.currentTime - initialTime)
        }
    }

    fun onResume() {
        if (remainingTime != NONE) {
            set(remainingTime)
        }
    }

    fun onStop() {
        release()
        clean()
    }

    fun set(delay: Long) {
        if (!isSet) {
            task.schedule(delay)
            isSet = true
        }
    }

    fun release() {
        task.unregister()
        isSet = false
    }

    fun clean() {
        requiredDelay = NONE
        initialTime = NONE
        remainingTime = NONE
        isSet = false
    }

    companion object {
        private const val NONE = Long.MIN_VALUE
    }
}