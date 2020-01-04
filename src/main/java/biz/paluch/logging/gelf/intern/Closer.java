package biz.paluch.logging.gelf.intern;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Internal helper to silently close resources.
 *
 * @author Mark Paluch
 * @since 17.07.14 10:45
 */
public class Closer {

    private Closer() {
        // no instance allowed
    }

    /**
     * Close silently the closeable.
     *
     * @param closeable the closeable
     */
    public static final void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Close silently the socket.
     *
     * @param socket the socket
     */
    public static final void close(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
