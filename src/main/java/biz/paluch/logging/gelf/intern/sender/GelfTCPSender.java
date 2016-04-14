package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * @author https://github.com/t0xa/gelfj
 * @author Mark Paluch
 */
public class GelfTCPSender implements GelfSender {

    public final static String CONNECTION_TIMEOUT = "connectionTimeout";
    public final static String READ_TIMEOUT = "readTimeout";
    public final static String RETRIES = "deliveryAttempts";
    public final static String KEEPALIVE = "keepAlive";

    private boolean shutdown = false;
    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int deliveryAttempts;
    private final SocketChannel socketChannel;
    private final ErrorReporter errorReporter;

    private Object connectLock = new Object();

    /**
     * 
     * @param host
     * @param port
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @param errorReporter
     * @throws IOException
     */
    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
            throws IOException {
        this(host, port, connectTimeoutMs, readTimeoutMs, 1, false, errorReporter);
    }

    /**
     * 
     * @param host
     * @param port
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @param deliveryAttempts
     * @param keepAlive
     * @param errorReporter
     * @throws IOException
     */
    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, int deliveryAttempts,
            boolean keepAlive, ErrorReporter errorReporter) throws IOException {

        // validate first address succeeds.
        InetAddress.getByName(host);
        this.host = host;
        this.port = port;
        this.errorReporter = errorReporter;
        this.connectTimeoutMs = connectTimeoutMs;
        this.deliveryAttempts = deliveryAttempts < 1 ? Integer.MAX_VALUE : deliveryAttempts;

        this.socketChannel = createSocketChannel(readTimeoutMs, keepAlive);;
    }

    private SocketChannel createSocketChannel(int readTimeoutMs, boolean keepAlive) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setKeepAlive(keepAlive);
        socketChannel.socket().setSoTimeout(readTimeoutMs);
        return socketChannel;
    }

    /**
     * 
     * @param message the message
     * @return
     */
    public boolean sendMessage(GelfMessage message) {

        if (shutdown) {
            return false;
        }

        IOException exception = null;

        for (int i = 0; i < deliveryAttempts; i++) {
            try {

                // (re)-connect if necessary
                if (!socketChannel.isConnected()) {
                    synchronized (connectLock) {
                        connect();
                    }
                }

                socketChannel.write(message.toTCPBuffer());

                return true;
            } catch (IOException e) {
                if (socketChannel != null) {
                    try {
                        socketChannel.close();
                    } catch (IOException o_O) {
                        // ignore
                    }
                }
                exception = e;
            }
        }

        if (exception != null) {
            errorReporter.reportError(exception.getMessage(),
                    new IOException("Cannot send data to " + host + ":" + port, exception));
        }

        return false;
    }

    protected void connect() throws IOException {

        if (socketChannel.isConnected()) {
            return;
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        if (socketChannel.connect(inetSocketAddress)) {
            return;
        }

        long connectTimeoutLeft = TimeUnit.MILLISECONDS.toNanos(connectTimeoutMs);
        long waitTimeoutMs = 10;
        long waitTimeoutNs = TimeUnit.MILLISECONDS.toNanos(waitTimeoutMs);
        boolean connected;
        try {
            while (!(connected = socketChannel.finishConnect())) {
                Thread.sleep(waitTimeoutMs);
                connectTimeoutLeft -= waitTimeoutNs;

                if (connectTimeoutLeft <= 0) {
                    break;
                }
            }

            if (!connected) {
                throw new ConnectException("Connection timed out. Cannot connect to " + inetSocketAddress + " within "
                        + TimeUnit.NANOSECONDS.toMillis(connectTimeoutLeft) + "ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection interrupted", e);
        }

    }

    public void close() {
        shutdown = true;
        Closer.close(socketChannel);
    }
}
