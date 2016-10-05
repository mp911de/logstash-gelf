package biz.paluch.logging.gelf.intern.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class GelfTCPSenderTest {

    @Mock
    private ErrorReporter errorReporter;

    @Captor
    private ArgumentCaptor<Exception> captor;

    @Test
    public void connectionRefusedShouldReportException() throws Exception {

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", 65534, 100, 100, errorReporter);

        tcpSender.sendMessage(new GelfMessage());

        verify(errorReporter).reportError(anyString(), captor.capture());

        Exception exception = captor.getValue();
        assertEquals(IOException.class, exception.getClass());
        assertEquals(ConnectException.class, exception.getCause().getClass());
    }

    @Test
    public void connectionTimeoutShouldReportException() throws Exception {

        GelfTCPSender tcpSender = new GelfTCPSender("8.8.8.8", 65534, 100, 100, errorReporter);

        tcpSender.sendMessage(new GelfMessage());

        verify(errorReporter).reportError(anyString(), captor.capture());

        Exception exception = captor.getValue();
        assertEquals(IOException.class, exception.getClass());
        assertEquals(ConnectException.class, exception.getCause().getClass());
    }

    @Test
    public void connectionTimeoutShouldApply() throws Exception {

        long now = System.currentTimeMillis();
        GelfTCPSender tcpSender = new GelfTCPSender("8.8.8.8", 65534, 1000, 1000, errorReporter);
        tcpSender.sendMessage(new GelfMessage());

        long duration = System.currentTimeMillis() - now;
        assertTrue(duration > 500);
    }

    @Test(expected = UnknownHostException.class)
    public void unknownHostShouldThrowException() throws Exception {
        new GelfTCPSender("unknown.host.unknown", 65534, 100, 100, errorReporter);
    }

    @Test
    public void shouldOpenConnection() throws Exception {

        int port = randomPort();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.socket().bind(new InetSocketAddress(port));

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        GelfTCPSender spy = spy(tcpSender);

        spy.sendMessage(gelfMessage);

        verify(spy, times(2)).isConnected();
        verify(spy).connect();

        listener.close();
        spy.close();
    }

    @Test
    public void shouldSendDataToOpenPort() throws Exception {

        int port = randomPort();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.socket().bind(new InetSocketAddress(port));

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        tcpSender.sendMessage(gelfMessage);

        GelfTCPSender spy = spy(tcpSender);

        spy.sendMessage(gelfMessage);

        verify(spy).isConnected();
        verify(spy, never()).connect();

        listener.close();
        spy.close();
    }

    @Test
    public void socketSendBufferOverflowTest() throws Exception {

        final int TEST_DURATION_MILLIS = 3000;

        int port = randomPort();

        // some payload to increase size of GelfMessage
        final GelfMessage gelfMessage = new GelfMessage("" +
                "----------------------------------------------------------------------------------------------------" +
                "----------------------------------------------------------------------------------------------------" +
                "----------------------------------------------------------------------------------------------------" +
                "----------------------------------------------------------------------------------------------------" +
                "----------------------------------------------------------------------------------------------------" +
                "", "" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "====================================================================================================" +
                "", 1000, "info");
        gelfMessage.setHost("host");

        final ByteBuffer referenceMessage = gelfMessage.toTCPBuffer();
        final byte[] referenceMessageArray = referenceMessage.array();

        final AtomicInteger readBytesCount = new AtomicInteger();
        final AtomicLong lastReadTs = new AtomicLong();
        final AtomicBoolean running = new AtomicBoolean(true);

        final long startTs = System.currentTimeMillis();

        // local server, reads all data and checks gelf messages stream integrity
        final ServerSocket listener = new ServerSocket(port);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] arr = new byte[1024 * 1024];
                try {
                    Socket socket = listener.accept();
                    InputStream inputStream = socket.getInputStream();

                    int currIdx = 0;
                    int len;
                    while ((len = inputStream.read(arr)) != -1) {
                        long currentTimeMillis = System.currentTimeMillis();
                        if (currentTimeMillis > startTs + TEST_DURATION_MILLIS) {
                            running.set(false);
                        }

                        lastReadTs.set(currentTimeMillis);
                        readBytesCount.addAndGet(len);

                        // check GelfMessages integrity, stream of similar gelf messages
                        for (int i = 0; i < len; i++) {
                            if (referenceMessageArray[currIdx] != arr[i]) {
                                running.set(false);
                                Assert.fail("inconsistent message: '" + new String(arr, Math.max(0, i - 20),
                                        Math.min(40, len)) + "'");
                            }

                            if (++currIdx == referenceMessageArray.length) {
                                currIdx = 0;
                            }
                        }
                        // message processing delay
                        Thread.sleep(10);
                    }
                } catch (IOException e) {
                } catch (InterruptedException e) {
                }
            }
        });
        thread.start();

        // log senders
        final GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        final AtomicInteger sendMessagesCount = new AtomicInteger();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (running.get()) {
                    tcpSender.sendMessage(gelfMessage);
                    sendMessagesCount.incrementAndGet();
                }
            }
        };
        int writersCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        ExecutorService executorService = Executors.newFixedThreadPool(writersCount);
        List<Future> futures = new ArrayList<Future>();
        for (int i = 0; i < writersCount; i++) {
            futures.add(executorService.submit(runnable));
        }

        // waiting writer tasks to complete
        for (Future future : futures) {
            future.get();
        }
        tcpSender.close();
        executorService.shutdown();

        // waiting listener completes reading, 1 sec idle state
        while (System.currentTimeMillis() - lastReadTs.get() < 1000) {
            Thread.sleep(100);
        }

        listener.close();

        int msgLen = referenceMessage.capacity();
        Assert.assertEquals(sendMessagesCount.get() * msgLen, readBytesCount.get());
    }

    @Test
    public void shouldSendDataToClosedPort() throws Exception {

        int port = randomPort();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.socket().bind(new InetSocketAddress(port));

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        listener.socket().close();
        listener.close();

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");
        tcpSender.sendMessage(gelfMessage);

        GelfTCPSender spy = spy(tcpSender);

        spy.sendMessage(gelfMessage);

        verify(spy, times(2)).isConnected();
        verify(spy).connect();

        spy.close();
    }

    protected int randomPort() {
        Random random = new Random();
        return random.nextInt(50000) + 1024;
    }
}