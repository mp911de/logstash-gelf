package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class GelfTCPSenderIntegrationTests {

    private static final int PORT = 1234;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Queue<Socket> sockets = new LinkedBlockingQueue<Socket>();
    private volatile ServerSocket serverSocket;
    private volatile boolean loopActive = true;

    private Thread thread;

    @BeforeEach
    void setUp() throws Exception {

        serverSocket = new ServerSocket(PORT);
        serverSocket.setSoTimeout(10000);

        thread = new Thread("GelfTCPSenderIntegrationTest-server") {

            @Override
            public void run() {

                while (loopActive) {
                    try {

                        Thread.sleep(0);

                        if (serverSocket.isClosed()) {
                            continue;

                        }

                        Socket socket = serverSocket.accept();
                        sockets.add(socket);
                        socket.setKeepAlive(true);
                        InputStream inputStream = socket.getInputStream();

                        while (!socket.isClosed()) {
                            IOUtils.copy(inputStream, out);
                            Thread.sleep(1);

                            if (latch.getCount() == 0) {
                                socket.close();
                            }
                        }

                    } catch (IOException e) {
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
    }

    @AfterEach
    void tearDown() throws IOException {

        thread.interrupt();

        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    void simpleTransport() throws Exception {

        thread.start();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");
        ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();
        int size = byteBuffer.remaining();

        sender.sendMessage(gelfMessage);
        sender.close();

        loopActive = false;
        latch.countDown();

        thread.join();

        assertThat(out.size()).isEqualTo(size);
    }

    @Test
    void shouldRecoverFromBrokenPipe() throws Exception {

        thread.start();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");
        ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        sockets.poll().close();

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        sender.close();
    }

    @Test
    void shouldRecoverFromClosedPort() throws Exception {

        thread.start();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");
        ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        sockets.poll().close();
        serverSocket.close();

        assertThat(sender.sendMessage(gelfMessage)).isFalse();

        serverSocket = new ServerSocket(PORT);

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        sender.close();
    }

    static class SmallBufferTCPSender extends GelfTCPSender {

        SmallBufferTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
                throws IOException {
            super(host, port, connectTimeoutMs, readTimeoutMs, errorReporter);
        }

        @Override
        protected SocketChannel createSocketChannel(int readTimeoutMs, boolean keepAlive) throws IOException {
            SocketChannel socketChannel = super.createSocketChannel(readTimeoutMs, keepAlive);

            socketChannel.socket().setSendBufferSize(100);

            return socketChannel;
        }
    }
}
