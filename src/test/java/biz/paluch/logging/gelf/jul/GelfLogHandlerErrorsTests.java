package biz.paluch.logging.gelf.jul;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
public class GelfLogHandlerErrorsTests {

    public static final String THE_HOST = "the host";
    public static final LogRecord MESSAGE = new LogRecord(Level.INFO, "message");

    @Mock
    private ErrorManager errorManager;

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private GelfMessageAssembler assembler;

    private GelfLogHandler sut = new GelfLogHandler();

    @BeforeEach
    public void before() throws Exception {

        GelfSenderFactory.addGelfSenderProvider(senderProvider);
        sut.setErrorManager(errorManager);
    }

    @AfterEach
    public void after() {
        GelfSenderFactory.removeGelfSenderProvider(senderProvider);
        GelfSenderFactory.removeAllAddedSenderProviders();
    }

    @Test
    public void testRuntimeExceptionOnCreateSender() throws Exception {

        sut.setGraylogHost(THE_HOST);

        when(senderProvider.supports(anyString())).thenReturn(true);
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new IllegalStateException());

        sut.publish(MESSAGE);

        verify(errorManager, atLeast(1)).error(anyString(), any(IllegalStateException.class), anyInt());
    }

    @Test
    public void testInvalidMessage() {

        sut.publish(MESSAGE);

        verify(errorManager, atLeast(1)).error(anyString(), ArgumentMatchers.<Exception> isNull(), anyInt());
    }

    @Test
    public void testErrorOnSend() {

        sut.publish(MESSAGE);

        verify(errorManager, atLeast(1)).error(anyString(), ArgumentMatchers.<Exception> isNull(), anyInt());
    }

}
