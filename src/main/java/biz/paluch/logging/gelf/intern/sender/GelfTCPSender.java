package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * @author https://github.com/t0xa/gelfj
 * @author Mark Paluch
 */
public class GelfTCPSender extends AbstractNioSender<SocketChannel> implements GelfSender {

    public final static String CONNECTION_TIMEOUT = "connectionTimeout";
    public final static String READ_TIMEOUT = "readTimeout";
    public final static String RETRIES = "deliveryAttempts";
    public final static String KEEPALIVE = "keepAlive";

    private final int readTimeoutMs;
    private final int connectTimeoutMs;
    private final boolean keepAlive;
    private final int deliveryAttempts;

    private final Object ioLock = new Object();

    private final ThreadLocal<ByteBuffer> writeBuffers = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
    };

    /**
     * @param host the host, must not be {@literal null}.
     * @param port the port.
     * @param connectTimeoutMs connection timeout, in {@link TimeUnit#MILLISECONDS}.
     * @param readTimeoutMs read timeout, in {@link TimeUnit#MILLISECONDS}.
     * @param errorReporter the error reporter, must not be {@literal null}.
     * @throws IOException in case of I/O errors
     */
    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
            throws IOException {
        this(host, port, connectTimeoutMs, readTimeoutMs, 1, false, errorReporter);
    }

    /**
     * @param host the host, must not be {@literal null}.
     * @param port the port.
     * @param connectTimeoutMs connection timeout, in {@link TimeUnit#MILLISECONDS}.
     * @param readTimeoutMs read timeout, in {@link TimeUnit#MILLISECONDS}.
     * @param deliveryAttempts number of delivery attempts.
     * @param keepAlive {@literal true} to enable TCP keep-alive.
     * @param errorReporter the error reporter, must not be {@literal null}.
     * @throws IOException in case of I/O errors
     */
    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, int deliveryAttempts,
            boolean keepAlive, ErrorReporter errorReporter) throws IOException {

        super(errorReporter, host, port);

        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.keepAlive = keepAlive;
        this.deliveryAttempts = deliveryAttempts < 1 ? Integer.MAX_VALUE : deliveryAttempts;

        this.setChannel(createSocketChannel(readTimeoutMs, keepAlive));
    }

    protected SocketChannel createSocketChannel(int readTimeoutMs, boolean keepAlive) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.socket().setKeepAlive(keepAlive);
        socketChannel.socket().setSoTimeout(readTimeoutMs);
        return socketChannel;
    }

    /**
     * @param message the message
     * @return {@literal true} if message was sent.
     */
    public boolean sendMessage(GelfMessage message) {

        if (isShutdown()) {
            return false;
        }

        IOException exception = null;

        for (int i = 0; i < deliveryAttempts; i++) {
            try {

                // (re)-connect if necessary
                if (!isConnected()) {
                    synchronized (ioLock) {
                        connect();
                    }
                }
                ByteBuffer buffer;
                if (BUFFER_SIZE == 0) {
                    buffer = message.toTCPBuffer();
                } else {
                    buffer = message.toTCPBuffer(getByteBuffer());
                }

                synchronized (ioLock) {
					write(buffer);
				}

                return true;
            } catch (IOException e) {
                Closer.close(channel());
                exception = e;
            }
        }

        if (exception != null) {
            reportError(exception.getMessage(),
                    new IOException("Cannot send data to " + getHost() + ":" + getPort(), exception));
        }

        return false;
    }


    protected void write(ByteBuffer buffer) throws IOException {

    	while (buffer.hasRemaining()) {
			int written = channel().write(buffer);

			if (written < 0) {
				// indicator the socket was closed
				Closer.close(channel());
				throw new SocketException("Cannot write buffer to channel");
			}
		}
	}

	protected ByteBuffer getByteBuffer() {
        ByteBuffer byteBuffer = writeBuffers.get();
        byteBuffer.clear();
        return byteBuffer;
    }

    protected boolean connect() throws IOException {

        if (isConnected()) {
            return false;
        }

        if (!channel().isOpen()) {
            Closer.close(channel());
            setChannel(createSocketChannel(readTimeoutMs, keepAlive));
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(getHost(), getPort());
        if (channel().connect(inetSocketAddress)) {
            return true;
        }

        long connectTimeoutLeft = TimeUnit.MILLISECONDS.toNanos(connectTimeoutMs);
        long waitTimeoutMs = 10;
        long waitTimeoutNs = TimeUnit.MILLISECONDS.toNanos(waitTimeoutMs);
        boolean connected;
        try {
            while (!(connected = channel().finishConnect())) {
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

			return connected;
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection interrupted", e);
        }
    }

    @Override
    protected boolean isConnected(SocketChannel channel) {
        return channel.isConnected();
    }
}
