package bazaar.tech.library.location.locationmanager.helper

import bazaar.tech.library.location.helper.LogUtils.enable
import bazaar.tech.library.location.helper.LogUtils.logD
import bazaar.tech.library.location.helper.LogUtils.logE
import bazaar.tech.library.location.helper.LogUtils.logI
import bazaar.tech.library.location.helper.LogUtils.logV
import bazaar.tech.library.location.helper.LogUtils.logW
import bazaar.tech.library.location.helper.LogUtils.setLogger
import bazaar.tech.library.location.helper.logging.Logger
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.VerificationModeFactory

class LogUtilsTest {
    @Mock
    lateinit var mockLogger: Logger

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        setLogger(mockLogger)
    }

    @Test
    fun whenLoggingIsDisabledItShouldNotForwardToLogger() {
        enable(false)
        logD("Dmessage")
        logE("Emessage")
        logI("Imessage")
        logV("Vmessage")
        logW("Wmessage")
        Mockito.verifyZeroInteractions(mockLogger)
    }

    @Test
    fun whenLoggingIsEnabledItShouldForwardToLogger() {
        enable(true)
        logD("Dmessage")
        logE("Emessage")
        logI("Imessage")
        logV("Vmessage")
        logW("Wmessage")
        Mockito.verify(mockLogger, VerificationModeFactory.times(1)).logD(ArgumentMatchers.anyString(), ArgumentMatchers.eq("Dmessage"))
        Mockito.verify(mockLogger, VerificationModeFactory.times(1)).logE(ArgumentMatchers.anyString(), ArgumentMatchers.eq("Emessage"))
        Mockito.verify(mockLogger, VerificationModeFactory.times(1)).logI(ArgumentMatchers.anyString(), ArgumentMatchers.eq("Imessage"))
        Mockito.verify(mockLogger, VerificationModeFactory.times(1)).logV(ArgumentMatchers.anyString(), ArgumentMatchers.eq("Vmessage"))
        Mockito.verify(mockLogger, VerificationModeFactory.times(1)).logW(ArgumentMatchers.anyString(), ArgumentMatchers.eq("Wmessage"))
    }

    @Test
    fun whenChangingLoggerItShouldLogIntoIt() {
        enable(true)
        val newLogger = Mockito.mock(Logger::class.java)
        setLogger(newLogger)
        logD("Dmessage")
        Mockito.verify(newLogger, VerificationModeFactory.times(1)).logD(ArgumentMatchers.anyString(), ArgumentMatchers.eq("Dmessage"))
        Mockito.verify(mockLogger, VerificationModeFactory.times(0)).logD(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }
}