package bazaar.tech.library.location.locationmanager.helper.continuoustask

import bazaar.tech.library.location.helper.continuoustask.ContinuousTask
import bazaar.tech.library.location.helper.continuoustask.ContinuousTaskScheduler
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ContinuousTaskSchedulerTest {
    @Mock
    lateinit var continuousTask: ContinuousTask
    private var continuousTaskScheduler: ContinuousTaskScheduler? = null
    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        continuousTaskScheduler = ContinuousTaskScheduler(continuousTask!!)
        Mockito.`when`(continuousTask!!.currentTime).thenReturn(INITIAL_TIME)
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedNotCalledIsSetShouldReturnFalse() {
        Assertions.assertThat(continuousTaskScheduler!!.isSet).isFalse
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedCalledIsSetShouldReturnTrue() {
        continuousTaskScheduler!!.delayed(0)
        Assertions.assertThat(continuousTaskScheduler!!.isSet).isTrue
    }

    @Test
    @Throws(Exception::class)
    fun whenOnPauseCalledIsSetShouldReturnFalse() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.onPause()
        Assertions.assertThat(continuousTaskScheduler!!.isSet).isFalse
    }

    @Test
    @Throws(Exception::class)
    fun whenOnResumeCalledIsSetShouldReturnTrue() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.onResume()
        Assertions.assertThat(continuousTaskScheduler!!.isSet).isTrue
    }

    @Test
    @Throws(Exception::class)
    fun whenOnStopCalledIsSetShouldReturnFalse() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.onStop()
        Assertions.assertThat(continuousTaskScheduler!!.isSet).isFalse
    }

    @Test
    @Throws(Exception::class)
    fun whenCleanCalledIsSetShouldReturnFalse() {
        continuousTaskScheduler!!.delayed(0)
        continuousTaskScheduler!!.clean()
        Assertions.assertThat(continuousTaskScheduler!!.isSet).isFalse
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedNotCalledTaskShouldHaveNoInteractionOnPauseAndResume() {
        continuousTaskScheduler!!.onPause()
        Mockito.verify(continuousTask, Mockito.never()).unregister()
        continuousTaskScheduler!!.set(0)
        Mockito.verify(continuousTask, Mockito.never()).delayed(0)
    }

    @Test
    @Throws(Exception::class)
    fun whenDelayedCalledTaskShouldSchedule() {
        continuousTaskScheduler!!.delayed(DELAY)
        Mockito.verify(continuousTask).schedule(DELAY)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnPauseCalledTaskShouldUnregister() {
        continuousTaskScheduler!!.delayed(DELAY)
        continuousTaskScheduler!!.onPause()
        Mockito.verify(continuousTask).unregister()
    }

    @Test
    @Throws(Exception::class)
    fun whenOnResumeCalledTaskShouldReScheduled() {
        continuousTaskScheduler!!.delayed(DELAY)
        Mockito.verify(continuousTask).schedule(DELAY)
        Mockito.`when`(continuousTask!!.currentTime).thenReturn(INITIAL_TIME + DURATION)
        continuousTaskScheduler!!.onPause()
        Mockito.verify(continuousTask).unregister()
        continuousTaskScheduler!!.onResume()
        Mockito.verify(continuousTask).schedule(DELAY - DURATION)
    }

    @Test
    @Throws(Exception::class)
    fun whenOnStopCalledTaskShouldHaveNoInteractionOnPauseAndResume() {
        continuousTaskScheduler!!.delayed(0)
        Mockito.verify(continuousTask).currentTime
        Mockito.verify(continuousTask).schedule(0)
        continuousTaskScheduler!!.onStop()
        Mockito.verify(continuousTask).unregister()
        continuousTaskScheduler!!.onPause()
        continuousTaskScheduler!!.onResume()
        Mockito.verifyNoMoreInteractions(continuousTask)
    }

    @Test
    @Throws(Exception::class)
    fun whenCleanCalledTaskShouldHaveNoInteractionOnPauseAndResume() {
        continuousTaskScheduler!!.delayed(0)
        Mockito.verify(continuousTask).currentTime
        Mockito.verify(continuousTask).schedule(0)
        continuousTaskScheduler!!.clean()
        continuousTaskScheduler!!.onPause()
        continuousTaskScheduler!!.onResume()
        Mockito.verifyNoMoreInteractions(continuousTask)
    }

    @Test
    @Throws(Exception::class)
    fun whenTaskIsAlreadyScheduledOnResumeShouldHaveNoInteraction() {
        continuousTaskScheduler!!.delayed(0)
        Mockito.verify(continuousTask).currentTime
        Mockito.verify(continuousTask).schedule(0)
        continuousTaskScheduler!!.onResume()
        Mockito.verifyNoMoreInteractions(continuousTask)
    }

    companion object {
        private const val INITIAL_TIME = 10000L
        private const val DELAY = 1000L
        private const val DURATION = 50L
    }
}