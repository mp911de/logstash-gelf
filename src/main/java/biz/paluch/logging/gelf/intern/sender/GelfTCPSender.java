package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfTCPSender implements GelfSender {

    public final static String CONNECTION_TIMEOUT = "connectionTimeout";
    public final static String READ_TIMEOUT = "readTimeout";
    public final static String RETRIES = "deliveryAttempts";
    public final static String KEEPALIVE = "keepAlive";

    private boolean shutdown = false;
    private InetAddress host;
    private int port;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private int deliveryAttempts;
    private boolean keepAlive;
    private Socket socket;
    private ErrorReporter errorReporter;

    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
            throws IOException {
        this(host, port, connectTimeoutMs, readTimeoutMs, 1, false, errorReporter);
    }

    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, int deliveryAttempts,
                         boolean keepAlive, ErrorReporter errorReporter) throws IOException {
        this.keepAlive = keepAlive;
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.errorReporter = errorReporter;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.deliveryAttempts = deliveryAttempts < 1 ? Integer.MAX_VALUE : deliveryAttempts;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown) {
            return false;
        }

        IOException exception = null;

        for (int i = 0; i < deliveryAttempts; i++) {
            try {
                // reconnect if necessary
                if (socket == null) {
                    socket = createSocket();
                }

                socket.getOutputStream().write(message.toTCPBuffer().array());
                return true;
            } catch (IOException e) {
                exception = e;
                // if an error occours, signal failure
                socket = null;
            }
        }

        if (exception != null) {
            errorReporter.reportError(exception.getMessage(), new IOException("Cannot send data to " + host + ":" + port,
                    exception));
        }

        return false;
    }

    protected Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(readTimeoutMs);
        socket.setKeepAlive(keepAlive);
        socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
        return socket;
    }

    public void close() {
        shutdown = true;
        Closer.close(socket);
    }
}
