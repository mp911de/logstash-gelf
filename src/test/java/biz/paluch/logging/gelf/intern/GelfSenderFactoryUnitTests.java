package biz.paluch.logging.gelf.intern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import external.MockitoExtension;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
public class GelfSenderFactoryUnitTests {

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

    @BeforeEach
    public void before() throws Exception {
        GelfSenderFactory.addGelfSenderProvider(senderProvider);

        when(assembler.getHost()).thenReturn(THE_HOST);
    }

    @AfterEach
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

        assertThat(result).isSameAs(sender);
    }

    @Test
    public void testCreateSenderFailUdp() throws Exception {

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();
    }

    @Test
    public void testCreateSenderFailTcp() throws Exception {

        reset(assembler);
        when(assembler.getHost()).thenReturn("tcp:" + THE_HOST);
        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();
    }

    @Test
    public void testCreateSenderFailUnknownHostException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new UnknownHostException());

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();

        verify(errorReporter).reportError(anyString(), any(UnknownHostException.class));

    }

    @Test
    public void testCreateSenderFailSocketException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new SocketException());

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();

        verify(errorReporter).reportError(anyString(), any(SocketException.class));

    }

    @Test
    public void testCreateSenderFailIOException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new IOException());

        GelfSender result = sut.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();

        verify(errorReporter).reportError(anyString(), any(IOException.class));

    }

    @Test
    public void testCreateSenderFailNPE() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new NullPointerException());

        sut.createSender(assembler, errorReporter, new HashMap<String, Object>());

    }

    private void mockSupports() {
        when(senderProvider.supports(THE_HOST)).thenReturn(true);
    }
}
