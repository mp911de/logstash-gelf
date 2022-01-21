package biz.paluch.logging.gelf.intern.sender;

import static org.mockito.Mockito.*;

import java.net.DatagramSocket;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Unit tests for {@link GelfUDPSender}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfUDPSenderUnitTests {

    @Mock
    private ErrorReporter errorReporter;

    @Captor
    private ArgumentCaptor<Exception> captor;

    @Test
    void unreachablePacketsShouldBeDiscardedSilently() throws Exception {

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", 65534, errorReporter);

        udpSender.sendMessage(new GelfMessage());

        verifyNoInteractions(errorReporter);
    }

    @Test
    void unknownHostShouldThrowException() throws Exception {

        new GelfUDPSender("unknown.host.unknown", 65534, errorReporter);
        verify(errorReporter).reportError(anyString(), any(Exception.class));
    }

    @Test
    void shouldSendDataToOpenPort() throws Exception {

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
    void shouldSendDataToClosedPort() throws Exception {

        int port = randomPort();

        DatagramSocket socket = new DatagramSocket(port);

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", port, errorReporter);
        socket.close();

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        udpSender.sendMessage(gelfMessage);

        GelfUDPSender spy = spy(udpSender);
        doReturn(true).when(spy).isConnected();

        spy.sendMessage(gelfMessage);

        verify(spy).isConnected();
        verify(spy, never()).connect();

        spy.close();
    }

    int randomPort() {
        Random random = new Random();
        return random.nextInt(50000) + 1024;
    }

}
