package biz.paluch.logging.gelf.intern.sender;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class GelfUDPSenderTest {

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
}