package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class GelfTCPSenderIntegrationTests {

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void name() throws Exception {

        final ServerSocket serverSocket = new ServerSocket(1234);
        final CountDownLatch latch = new CountDownLatch(1);
        serverSocket.setSoTimeout(1000);

        Thread thread = new Thread("GelfTCPSenderIntegrationTest-server") {

            @Override
            public void run() {

                try {
                    Socket socket = serverSocket.accept();
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
                    e.printStackTrace();
                } catch (InterruptedException e) {
                }
            }
        };

        try {
            thread.start();

            SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", 1234, 1000, 1000, new ErrorReporter() {
                @Override
                public void reportError(String message, Exception e) {
                }
            });

            GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), 1234, "7");
            ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();
            int size = byteBuffer.remaining();

            sender.sendMessage(gelfMessage);
            sender.close();

            latch.countDown();
            thread.join();

            assertThat(out.size()).isEqualTo(size);

        } finally {
            thread.interrupt();
        }

    }

    static class SmallBufferTCPSender extends GelfTCPSender {

        public SmallBufferTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
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
