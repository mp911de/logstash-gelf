package biz.paluch.logging.gelf.logback;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import external.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GelfLogbackAppenderErrorsUnitTests {

    public static final String THE_HOST = "the host";

    public static final LoggingEvent LOGGING_EVENT = new LoggingEvent("my.class", new LoggerContext().getLogger("my.class"),
            Level.INFO, "message", null, null);

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private Context context;

    @Mock
    private StatusManager statusManager;

    private GelfLogbackAppender sut = new GelfLogbackAppender();

    @BeforeEach
    public void before() throws Exception {
        GelfSenderFactory.addGelfSenderProvider(senderProvider);

        sut.setContext(context);
        when(context.getStatusManager()).thenReturn(statusManager);

        LOGGING_EVENT.setCallerData(new StackTraceElement[] { new StackTraceElement("a", "b", "c", 1) });

    }

    @AfterEach
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
