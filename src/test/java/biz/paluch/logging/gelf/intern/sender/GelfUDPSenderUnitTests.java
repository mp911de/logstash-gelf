package biz.paluch.logging.gelf.intern.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import external.MockitoExtension;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
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

    @Test
    public void unknownHostShouldThrowException() throws Exception {

        assertThrows(UnknownHostException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                new GelfUDPSender("unknown.host.unknown", 65534, errorReporter);
            }
        });
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
        doReturn(true).when(spy).isConnected();

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
