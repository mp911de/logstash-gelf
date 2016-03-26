package biz.paluch.logging.gelf.intern;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import biz.paluch.logging.gelf.GelfMessageAssembler;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class GelfSenderFactoryTest {

    public static final String THE_HOST = "thehost";

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private GelfSender sender;

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private GelfMessageAssembler assembler;

    private GelfSenderFactory sut = new GelfSenderFactory();

    @Before
    public void before() throws Exception {
        GelfSenderFactory.addGelfSenderProvider(senderProvider);

        when(assembler.getHost()).thenReturn(THE_HOST);
    }

    @After
    public void after() throws Exception {
        GelfSenderFactory.removeGelfSenderProvider(senderProvider);
        GelfSenderFactory.removeAllAddedSenderProviders();
    }

    @Test
    public void testCreateSender() throws Exception {

        when(assembler.getHost()).thenReturn(THE_HOST);
        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenReturn(sender);

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);

        assertSame(sender, result);
    }

    @Test
    public void testCreateSenderFailUdp() throws Exception {

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertNull(result);
    }

    @Test
    public void testCreateSenderFailTcp() throws Exception {

        reset(assembler);
        when(assembler.getHost()).thenReturn("tcp:" + THE_HOST);
        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertNull(result);
    }

    @Test
    public void testCreateSenderFailUnknownHostException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new UnknownHostException());

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertNull(result);

        verify(errorReporter).reportError(anyString(), any(UnknownHostException.class));

    }

    @Test
    public void testCreateSenderFailSocketException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new SocketException());

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertNull(result);

        verify(errorReporter).reportError(anyString(), any(SocketException.class));

    }

    @Test
    public void testCreateSenderFailIOException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new IOException());

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertNull(result);

        verify(errorReporter).reportError(anyString(), any(IOException.class));

    }

    @Test(expected = NullPointerException.class)
    public void testCreateSenderFailNPE() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new NullPointerException());

        sut.createSender(assembler, errorReporter, new HashMap<String, Object>());

    }

    private void mockSupports() {
        when(senderProvider.supports(THE_HOST)).thenReturn(true);
    }
}
