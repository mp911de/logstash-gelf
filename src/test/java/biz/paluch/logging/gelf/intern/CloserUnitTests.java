package biz.paluch.logging.gelf.intern;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class CloserUnitTests {

    @Mock
    private InputStream inputStream;

    @Mock
    private Socket socket;

    @Test
    void closeSocketShouldClose() throws Exception {

        Closer.close(socket);

        verify(socket).close();
    }

    @Test
    void closeSocketDoesNotFailOnNull() throws Exception {

        Closer.close((Socket) null);
    }

    @Test
    void closeSocketShouldNotPropagateExceptions() throws Exception {

        doThrow(new IOException()).when(socket).close();
        Closer.close(socket);
    }

    @Test
    void closeCloseableShouldClose() throws Exception {

        Closer.close(inputStream);

        verify(inputStream).close();
    }

    @Test
    void closeCloseableShouldNotPropagateExceptions() throws Exception {

        doThrow(new IOException()).when(inputStream).close();
        Closer.close(inputStream);
    }

    @Test
    void closeCloseableDoesNotFailOnNull() throws Exception {

        Closer.close((Closeable) null);
    }
}
