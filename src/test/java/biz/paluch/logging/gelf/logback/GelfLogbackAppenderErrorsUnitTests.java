package biz.paluch.logging.gelf.logback;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

@RunWith(MockitoJUnitRunner.class)
public class GelfLogbackAppenderErrorsUnitTests {

    public static final String THE_HOST = "the host";

    public static final LoggingEvent LOGGING_EVENT = new LoggingEvent("my.class", new LoggerContext().getLogger("my.class"),
            Level.INFO, "message", null, null);

    @Mock
    private GelfSender sender;

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private GelfMessageAssembler assembler;

    @Mock
    private Context context;

    @Mock
    private StatusManager statusManager;

    private GelfLogbackAppender sut = new GelfLogbackAppender();

    @Before
    public void before() throws Exception {
        GelfSenderFactory.addGelfSenderProvider(senderProvider);

        sut.setContext(context);
        when(context.getStatusManager()).thenReturn(statusManager);

        LOGGING_EVENT.setCallerData(new StackTraceElement[] { new StackTraceElement("a", "b", "c", 1) });

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

        verify(statusManager, atLeast(1)).add(any(Status.class));
    }

    @Test
    public void testInvalidMessage() throws Exception {

        sut.append(LOGGING_EVENT);

        verify(statusManager, atLeast(1)).add(any(Status.class));
    }

    @Test
    public void testErrorOnSend() throws Exception {

        sut.append(LOGGING_EVENT);

        verify(statusManager, atLeast(1)).add(any(Status.class));
    }
}
