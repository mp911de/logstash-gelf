package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * (c) Alekandar - https://github.com/salex89
 */

public class GelfHTTPSenderTest {


    @Mock
    HttpClientBuilder builder;
    @Mock
    CloseableHttpClient closeableHttpClient;
    @Mock
    CloseableHttpResponse closeableHttpResponse;
    @Mock
    ErrorReporter errorReporter;

    @Before
    public void prepareMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
//        when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 202, "Accepted"));
//        when(builder.build()).thenReturn(closeableHttpClient);
//        when(closeableHttpClient.execute((HttpUriRequest) any())).thenReturn(closeableHttpResponse);
    }

    @Test
    public void sendMessageTest() throws IOException {
        when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 202, "Accepted"));
        when(builder.build()).thenReturn(closeableHttpClient);
        when(closeableHttpClient.execute((HttpUriRequest) any())).thenReturn(closeableHttpResponse);

        String uri = "http://192.168.0.100/gelf";
        GelfHTTPSender sender = new GelfHTTPSender(uri, 12201, errorReporter, builder);
        GelfMessage gelfMessage = new GelfMessage();
        boolean success = sender.sendMessage(gelfMessage);
        verify(builder, times(1)).build();
        assertTrue(success);
        verifyZeroInteractions(errorReporter);
        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(closeableHttpClient).execute(postCaptor.capture());
        HttpPost executedPost = postCaptor.getValue();
        assertEquals("http://192.168.0.100:12201/gelf", executedPost.getURI().toString());
        assertEquals(gelfMessage.toJson(), EntityUtils.toString(executedPost.getEntity()));
    }

    @Test
    public void sendMessageTestIncorrectUrl() throws IOException {
        String uri = "adda";
        GelfHTTPSender sender = new GelfHTTPSender(uri, 12201, errorReporter, HttpClientBuilder.create());
        GelfMessage gelfMessage = new GelfMessage();
        boolean success = sender.sendMessage(gelfMessage);
        assertFalse(success);
//        verifyZeroInteractions(errorReporter);
//        ArgumentCaptor<HttpPost> postCaptor = ArgumentCaptor.forClass(HttpPost.class);
//        verify(closeableHttpClient).execute(postCaptor.capture());
//        HttpPost executedPost = postCaptor.getValue();
//        assertEquals("http://192.168.0.100:12201/gelf", executedPost.getURI().toString());
//        assertEquals(gelfMessage.toJson(), EntityUtils.toString(executedPost.getEntity()));
    }
}
