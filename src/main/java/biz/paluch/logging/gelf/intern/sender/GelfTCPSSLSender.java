package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

import biz.paluch.logging.gelf.intern.ErrorReporter;

/**
 * TCP with SSL {@link biz.paluch.logging.gelf.intern.GelfSender}.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 1.11
 */
public class GelfTCPSSLSender extends GelfTCPSender {

    private final int connectTimeoutMs;
    private final SSLContext sslContext;
    private final ThreadLocal<ByteBuffer> sslNetworkBuffers = new ThreadLocal<ByteBuffer>();
    private final ThreadLocal<ByteBuffer> tempBuffers = new ThreadLocal<ByteBuffer>();

    private volatile SSLEngine sslEngine;

    private volatile SSLSession sslSession;

    /**
     * @param host the host, must not be {@literal null}.
     * @param port the port.
     * @param connectTimeoutMs connection timeout, in {@link TimeUnit#MILLISECONDS}.
     * @param readTimeoutMs read timeout, in {@link TimeUnit#MILLISECONDS}.
     * @param deliveryAttempts number of delivery attempts.
     * @param keepAlive {@literal true} to enable TCP keep-alive.
     * @param errorReporter the error reporter, must not be {@literal null}.
     * @param sslContext the SSL context, must not be {@literal null}.
     * @throws IOException in case of I/O errors
     */
    public GelfTCPSSLSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, int deliveryAttempts,
            boolean keepAlive, ErrorReporter errorReporter, SSLContext sslContext) throws IOException {

        super(host, port, connectTimeoutMs, readTimeoutMs, deliveryAttempts, keepAlive, errorReporter);

        this.connectTimeoutMs = connectTimeoutMs;
        this.sslContext = sslContext;
    }

    @Override
    protected boolean connect() throws IOException {

        this.sslEngine = sslContext.createSSLEngine();
        this.sslEngine.setUseClientMode(true);
        this.sslSession = sslEngine.getSession();

        if (super.connect()) {
            // Begin handshake
            sslEngine.beginHandshake();
            doHandshake(channel(), sslEngine, ByteBuffer.allocate(sslSession.getPacketBufferSize()),
                    ByteBuffer.allocate(sslSession.getPacketBufferSize()));
        }
        return false;
    }

    protected boolean isConnected() throws IOException {

        SocketChannel socketChannel = channel();

        return socketChannel != null && socketChannel.isOpen() && isConnected(socketChannel);
    }

    @Override
    protected void write(ByteBuffer gelfBuffer) throws IOException {

        while (gelfBuffer.hasRemaining()) {

            read();

            ByteBuffer myNetData = getNetworkBuffer();
            // Generate SSL/TLS encoded data (handshake or application data)
            gelfBuffer.mark();
            SSLEngineResult res = sslEngine.wrap(gelfBuffer, myNetData);

            // Process status of call
            if (res.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                this.sslNetworkBuffers.set(enlargeBuffer(gelfBuffer, myNetData));
                gelfBuffer.reset();
            }

            if (res.getStatus() == SSLEngineResult.Status.OK) {
                myNetData.flip();

                // Send SSL/TLS encoded data to peer
                while (myNetData.hasRemaining()) {
                    int written = channel().write(myNetData);
                    if (written == -1) {
                        throw new SocketException("Channel closed");
                    }
                }
            }
        }
    }

    private void read() throws IOException {

        ByteBuffer myNetData = getNetworkBuffer();
        ByteBuffer tempBuffer = getTempBuffer();

        if (channel().read(myNetData) < 0) {
            throw new SocketException("Channel closed");
        }

        // Process incoming handshaking data
        myNetData.flip();
        sslEngine.unwrap(myNetData, tempBuffer);
    }

    private ByteBuffer getNetworkBuffer() {

        ByteBuffer networkBuffer = this.sslNetworkBuffers.get();
        if (networkBuffer == null) {
            this.sslNetworkBuffers.set(networkBuffer = ByteBuffer.allocateDirect(sslSession.getPacketBufferSize()));
        }
        networkBuffer.clear();
        return networkBuffer;
    }

    private ByteBuffer getTempBuffer() {

        ByteBuffer tempBuffer = this.tempBuffers.get();
        if (tempBuffer == null) {
            this.tempBuffers.set(tempBuffer = ByteBuffer.allocateDirect(sslSession.getApplicationBufferSize()));
        }
        tempBuffer.clear();
        return tempBuffer;
    }

    private ByteBuffer enlargeBuffer(ByteBuffer src, ByteBuffer dst) {

        // Could attempt to drain the dst buffer of any already obtained
        // data, but we'll just increase it to the size needed.
        ByteBuffer buffer = ByteBuffer.allocate(dst.capacity() + src.remaining());
        dst.flip();
        buffer.put(dst);
        return buffer;
    }

    private void doHandshake(SocketChannel socketChannel, SSLEngine sslEngine, ByteBuffer myNetData, ByteBuffer peerNetData)
            throws IOException {

        // Create byte buffers to use for holding application data
        int appBufferSize = sslEngine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
        long handshakeBegin = System.currentTimeMillis();

        SSLEngineResult.HandshakeStatus hs = sslEngine.getHandshakeStatus();

        // Process handshaking message
        while (hs != SSLEngineResult.HandshakeStatus.FINISHED && hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            long handshakeDuration = System.currentTimeMillis() - handshakeBegin;

            if (handshakeDuration > connectTimeoutMs) {
                throw new SocketTimeoutException("SSL handshake timeout exceeded");
            }

            if (!isConnected(socketChannel)) {
                throw new SocketException("Channel closed");
            }

            switch (hs) {

                case NEED_UNWRAP:
                    // Receive handshaking data from peer
                    if (socketChannel.read(peerNetData) < 0) {
                        throw new SocketException("Channel closed");
                    }

                    // Process incoming handshaking data
                    peerNetData.flip();
                    SSLEngineResult res = sslEngine.unwrap(peerNetData, peerAppData);
                    peerNetData.compact();
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK:
                            // Handle OK status
                            break;
                        case BUFFER_OVERFLOW:
                            peerAppData = enlargeBuffer(peerNetData, peerAppData);
                            break;

                        case BUFFER_UNDERFLOW:

                            break;
                    }
                    break;

                case NEED_WRAP:
                    // Empty the local network packet buffer.
                    myNetData.clear();

                    // Generate handshaking data
                    res = sslEngine.wrap(myAppData, myNetData);
                    hs = res.getHandshakeStatus();

                    // Check status
                    switch (res.getStatus()) {
                        case OK:
                            myNetData.flip();

                            // Send the handshaking data to peer
                            while (myNetData.hasRemaining()) {
                                if (socketChannel.write(myNetData) < 0) {
                                    // Handle closed channel
                                }
                            }
                            break;
                        case BUFFER_OVERFLOW:
                            myNetData = enlargeBuffer(myAppData, myNetData);
                            break;

                        case BUFFER_UNDERFLOW:
                            break;
                        case CLOSED:

                            if (sslEngine.isOutboundDone()) {
                                return;
                            } else {
                                sslEngine.closeOutbound();
                                hs = sslEngine.getHandshakeStatus();
                                break;
                            }

                        default:
                            throw new IOException("Cannot wrap data: " + res.getStatus());

                    }
                    break;

                case NEED_TASK:
                    Runnable task;
                    while ((task = sslEngine.getDelegatedTask()) != null) {
                        task.run();
                    }
                    hs = sslEngine.getHandshakeStatus();
                    break;

                case FINISHED:
                    return;
            }
        }
    }

    @Override
    public void close() {

        if (channel() != null) {

            try {
                closeSocketChannel();
            } catch (IOException e) {
                reportError(e.getMessage(), e);
            }
        }

        super.close();
    }

    @Override
    protected boolean isConnected(SocketChannel channel) {
        return super.isConnected(channel) && channel.isOpen();
    }

    private void closeSocketChannel() throws IOException {

        if (sslEngine != null) {

            sslEngine.closeOutbound();

            if (sslSession != null) {
                doHandshake(channel(), sslEngine, ByteBuffer.allocate(sslSession.getPacketBufferSize()),
                        ByteBuffer.allocate(sslSession.getPacketBufferSize()));
            }
        }
    }
}
