package biz.paluch.logging.gelf.log4j;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class GelfLogAppenderErrorsUnitTests {

    public static final String THE_HOST = "the host";

    public static final LoggingEvent LOGGING_EVENT = new LoggingEvent("my.class", Logger.getLogger("my.class"),
            org.apache.log4j.Level.INFO, "message", null);

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private GelfMessageAssembler assembler;

    private GelfLogAppender sut = new GelfLogAppender();

    @Before
    public void before() throws Exception {

        GelfSenderFactory.addGelfSenderProvider(senderProvider);

        sut.setErrorHandler(errorHandler);
    }

    @After
    public void after() throws Exception {
        GelfSenderFactory.removeGelfSenderProvider(senderProvider);
        GelfSenderFactory.removeAllAddedSenderProviders();
    }

    @Test
    public void testRuntimeExceptionOnCreateSender() throws Exception {
        sut.setGraylogHost(THE_HOST);

        sut.append(LOGGING_EVENT);

        verify(errorHandler, atLeast(1)).error(anyString(), ArgumentMatchers.<Exception> isNull(), anyInt());
    }

    @Test
    public void testInvalidMessage() throws Exception {

        sut.append(LOGGING_EVENT);

        verify(errorHandler, atLeast(1)).error(anyString(), ArgumentMatchers.<Exception> isNull(), anyInt());
    }

    @Test
    public void testErrorOnSend() throws Exception {

        sut.append(LOGGING_EVENT);

        verify(errorHandler, atLeast(1)).error(anyString(), ArgumentMatchers.<Exception> isNull(), anyInt());
    }

    @Test(timeout = 500)
    @Ignore("Flakey during to execution environment")
    public void gelfPortNotReachable() throws Exception {

        LogManager.getLoggerRepository().resetConfiguration();
        DOMConfigurator.configure(getClass().getResource("/log4j/log4j-gelf-not-reachable.xml"));

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOGGING_EVENT);
    }
}
