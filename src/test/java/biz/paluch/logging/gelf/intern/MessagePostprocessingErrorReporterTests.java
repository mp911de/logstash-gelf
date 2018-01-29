package biz.paluch.logging.gelf.intern;

import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import external.MockitoExtension;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class MessagePostprocessingErrorReporterTests {

    @Mock
    private ErrorReporter target;

    @Test
    void shouldRetainOriginalMessage() {

        IOException e = new IOException();
        new MessagePostprocessingErrorReporter(target).reportError("foo", e);

        verify(target).reportError("foo", e);
    }

    @Test
    void shouldReplaceNullMessageWithExceptionClassName() {

        IOException e = new IOException();
        new MessagePostprocessingErrorReporter(target).reportError(null, e);

        verify(target).reportError("IOException", e);
    }

    @Test
    void shouldReplaceNullMessageWithExceptionMessage() {

        IOException e = new IOException("foo");
        new MessagePostprocessingErrorReporter(target).reportError(null, e);

        verify(target).reportError("foo", e);
    }
}
