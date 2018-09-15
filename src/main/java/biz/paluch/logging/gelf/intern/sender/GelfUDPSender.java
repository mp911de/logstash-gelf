package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * @author https://github.com/t0xa/gelfj
 * @author Mark Paluch
 */
public class GelfUDPSender extends AbstractNioSender<DatagramChannel> implements GelfSender {

    private static final ThreadLocal<ByteBuffer> WRITE_BUFFERS = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);
        }
    };

    private static final ThreadLocal<ByteBuffer> TEMP_BUFFERS = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);
        }
    };

    private final Object ioLock = new Object();

    public GelfUDPSender(String host, int port, ErrorReporter errorReporter) throws IOException {
        super(errorReporter, host, port);
        connect();
    }

    public boolean sendMessage(GelfMessage message) {

        if (INITIAL_BUFFER_SIZE == 0) {
            return sendDatagrams(message.toUDPBuffers());
        }

        return sendDatagrams(GelfBuffers.toUDPBuffers(message, WRITE_BUFFERS, TEMP_BUFFERS));
    }

    private boolean sendDatagrams(ByteBuffer[] bytesList) {

        try {
            // (re)-connect if necessary
            if (!isConnected()) {
                synchronized (ioLock) {
                    connect();
                }
            }

            for (ByteBuffer buffer : bytesList) {

                synchronized (ioLock) {
                    while (buffer.hasRemaining()) {
                        channel().write(buffer);
                    }
                }
            }
        } catch (IOException e) {
            reportError(e.getMessage(), new IOException("Cannot send data to " + getHost() + ":" + getPort(), e));
            return false;
        }

        return true;
    }

    protected void connect() throws IOException {

        if (isConnected()) {
            return;
        }

        if (channel() == null) {
            setChannel(DatagramChannel.open());
        } else if (!channel().isOpen()) {
            Closer.close(channel());
            setChannel(DatagramChannel.open());
        }

        channel().configureBlocking(false);

        InetSocketAddress inetSocketAddress = new InetSocketAddress(getHost(), getPort());

        try {
            DatagramChannel connect = channel().connect(inetSocketAddress);
            setChannel(connect);
        } catch (SocketException e) {
            reportError(e.getMessage(), e);
        }
    }

    @Override
    protected boolean isConnected(DatagramChannel channel) {
        return channel.isConnected();
    }
}
