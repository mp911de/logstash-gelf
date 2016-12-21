package biz.paluch.logging.gelf.intern.sender;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class GelfUDPSenderUnitTests {

    @Mock
    private ErrorReporter errorReporter;

    @Captor
    private ArgumentCaptor<Exception> captor;

    @Test
    public void unreachablePacketsShouldBeDiscardedSilently() throws Exception {

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", 65534, errorReporter);

        udpSender.sendMessage(new GelfMessage());

        verifyZeroInteractions(errorReporter);
    }

    @Test(expected = UnknownHostException.class)
    public void unknownHostShouldThrowException() throws Exception {
        new GelfUDPSender("unknown.host.unknown", 65534, errorReporter);
    }

    @Test
    public void shouldSendDataToOpenPort() throws Exception {

        int port = randomPort();

        DatagramSocket socket = new DatagramSocket(port);

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", port, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        udpSender.sendMessage(gelfMessage);

        GelfUDPSender spy = spy(udpSender);

        spy.sendMessage(gelfMessage);

        verify(spy).isConnected();
        verify(spy, never()).connect();

        socket.close();
        spy.close();
    }

    @Test
    public void shouldSendDataToClosedPort() throws Exception {

        int port = randomPort();

        DatagramSocket socket = new DatagramSocket(port);

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", port, errorReporter);
        socket.close();

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        udpSender.sendMessage(gelfMessage);

        GelfUDPSender spy = spy(udpSender);

        spy.sendMessage(gelfMessage);

        verify(spy).isConnected();
        verify(spy, never()).connect();

        spy.close();
    }

    protected int randomPort() {
        Random random = new Random();
        return random.nextInt(50000) + 1024;
    }
}
