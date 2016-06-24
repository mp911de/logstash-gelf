package biz.paluch.logging.gelf.intern.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.util.Random;

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