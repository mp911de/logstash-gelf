package biz.paluch.logging.gelf.intern;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 17.07.14 10:45
 */
public class Closer {

    /**
     * Close silently the closeable.
     * 
     * @param closeable
     */
    public final static void close(Closeable closeable) {
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
     * @param socket
     */
    public final static void close(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
