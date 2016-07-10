package biz.paluch.logging.gelf.intern.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import biz.paluch.logging.gelf.NettyLocalHTTPServer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Aleksandar Stojadinovic
 */
@RunWith(MockitoJUnitRunner.class)
public class GelfHTTPSenderTest {

    public static final GelfMessage GELF_MESSAGE = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");
    private NettyLocalHTTPServer server;
    private GelfHTTPSender sender;

    @Mock
    ErrorReporter errorReporter;

    @Before
    public void setUp() throws Exception {
        server = new NettyLocalHTTPServer();
        server.run();
        
        String uri = "http://127.0.0.1:19393";
        sender = new GelfHTTPSender(new URL(uri), 1000, 1000, errorReporter);
    }

    @After
    public void tearDown() {
        server.close();
        sender.close();
    }

    @Test
    public void sendMessageTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121l, "WARNING");

        boolean success = sender.sendMessage(gelfMessage);

        assertTrue(success);
        verifyZeroInteractions(errorReporter);

        List<Object> jsonValues = server.getJsonValues();
        assertEquals(1, jsonValues.size());

        Map<String, Object> messageJson = (Map<String, Object>) jsonValues.get(0);
        assertEquals(gelfMessage.getShortMessage(), messageJson.get("short_message"));
        assertEquals(gelfMessage.getFullMessage(), messageJson.get("full_message"));
        assertEquals(gelfMessage.getTimestamp(), messageJson.get("timestamp"));
        assertEquals(gelfMessage.getLevel(), messageJson.get("level"));
    }

    @Test
    public void shouldUsePostHttpMethod() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        assertTrue(success);
        assertEquals("POST", server.getLastHttpRequest().name());
    }

    @Test
    public void shouldUseJsonContentType() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        assertTrue(success);
        assertEquals("application/json", server.getLastHttpHeaders().get("Content-type"));

    }

    @Test
    public void sendMessageFailureTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

        String uri = "http://127.0.0.1:19393";
        GelfHTTPSender sender = new GelfHTTPSender(new URL(uri), 1000, 1000, errorReporter);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        assertFalse(success);
        verify(errorReporter, times(1)).reportError(anyString(), any(Exception.class));
    }

}
