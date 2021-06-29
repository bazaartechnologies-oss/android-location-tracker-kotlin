package bazaar.tech.library.location.helper.continuoustask

import android.os.Handler

class ContinuousTask(private val taskId: String, private val continuousTaskRunner: ContinuousTaskRunner) : Handler(), Runnable {
    private val continuousTaskScheduler: ContinuousTaskScheduler = ContinuousTaskScheduler(this)

    interface ContinuousTaskRunner {
        /**
         * Callback to take action when scheduled time is arrived.
         * Called with given taskId in order to distinguish which task should be run,
         * in case of same [ContinuousTaskRunner] passed to multiple Tasks
         */
        fun runScheduledTask(taskId: String)
    }

    fun delayed(delay: Long) {
        continuousTaskScheduler.delayed(delay)
    }

    fun pause() {
        continuousTaskScheduler.onPause()
    }

    fun resume() {
        continuousTaskScheduler.onResume()
    }

    fun stop() {
        continuousTaskScheduler.onStop()
    }

    override fun run() {
        continuousTaskRunner.runScheduledTask(taskId)
    }

    fun schedule(delay: Long) {
        postDelayed(this, delay)
    }

    fun unregister() {
        removeCallbacks(this)
    }

    val currentTime: Long
        get() = System.currentTimeMillis()

}