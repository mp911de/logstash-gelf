package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import biz.paluch.logging.RuntimeContainerProperties;
import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;

/**
 * @author Mark Paluch
 */
public abstract class AbstractNioSender<T extends AbstractSelectableChannel & ByteChannel> implements ErrorReporter {

    /**
     * Buffer size for transmit buffers. Defaults to 99 * 8192
     */
    public static final String PROPERTY_BUFFER_SIZE = "logstash-gelf.buffer.size";

    protected final static int BUFFER_SIZE = Integer
            .parseInt(RuntimeContainerProperties.getProperty(PROPERTY_BUFFER_SIZE, "" + (99 * 8192)));

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

        if (channel() != null && channel().isOpen() && isConnected(channel()) && channel.read(byteBuffer) >= 0) {
            return true;
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
