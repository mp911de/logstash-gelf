package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import biz.paluch.logging.RuntimeContainerProperties;
import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;

/**
 * Base class for NIO-channel senders.
 *
 * @param <T> must by {@link AbstractSelectableChannel} and {@link ByteChannel}.
 * @author Mark Paluch
 * @since 1.11
 */
public abstract class AbstractNioSender<T extends AbstractSelectableChannel & ByteChannel> implements ErrorReporter {

    /**
     * Buffer size for transmit buffers. Defaults to 99 * 8192
     */
    public static final String PROPERTY_BUFFER_SIZE = "logstash-gelf.buffer.size";

    /**
     * Default initial buffer size {@code 40 x 8192}.
     */
    public static final int INITIAL_BUFFER_SIZE = Integer.parseInt(RuntimeContainerProperties.getProperty(PROPERTY_BUFFER_SIZE,
            "" + (40 * 8192)));

    private T channel;
    private volatile boolean shutdown = false;
    private final ErrorReporter errorReporter;
    private final String host;
    private final int port;

    private final ThreadLocal<ByteBuffer> readBuffers = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(1);
        }
    };

    /**
     * Create a new {@link AbstractNioSender} given {@link ErrorReporter}, {@code host} and {@code port}. Object creation
     * triggers hostname lookup for early failure.
     *
     * @param errorReporter the error reporter.
     * @param host hostname.
     * @param port port number.
     * @exception UnknownHostException if no IP address for the {@code host} could be found, or if a scope_id was specified for
     *            a global IPv6 address.
     */
    protected AbstractNioSender(ErrorReporter errorReporter, String host, int port) throws UnknownHostException {

        // validate first address succeeds.
        InetAddress.getByName(host);
        this.errorReporter = errorReporter;
        this.host = host;
        this.port = port;

    }

    protected boolean isConnected() throws IOException {

        ByteBuffer byteBuffer = readBuffers.get();
        byteBuffer.clear();

        T myChannel = channel();

        if (myChannel != null && myChannel.isOpen() && isConnected(myChannel)) {

            try {
                return myChannel.read(byteBuffer) >= 0;
            } catch (PortUnreachableException e) {
                errorReporter.reportError("Port " + getHost() + ":" + getPort() + " not reachable", e);
            } catch (IOException e) {
                errorReporter.reportError("Cannot verify whether channel to " + getHost() + ":" + getPort() + " is connected: "
                        + e.getMessage(), e);
            }
        }

        return false;
    }

    protected abstract boolean isConnected(T channel);

    protected T channel() {
        return channel;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void close() {
        shutdown = true;
        Closer.close(channel());
    }

    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void reportError(String message, Exception e) {
        errorReporter.reportError(message, e);
    }

    public void setChannel(T channel) {
        this.channel = channel;
    }
}
