package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.NettyLocalHTTPServer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.json.simple.JSONObject;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * (c) Alekandar - https://github.com/salex89
 */

public class GelfHTTPSenderTest {

    private NettyLocalHTTPServer server;
    private GelfHTTPSender sender;
    @Mock ErrorReporter errorReporter;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        server = new NettyLocalHTTPServer();
        server.run();
        String uri = "http://127.0.0.1:19393";
        sender = new GelfHTTPSender(new URL(uri), errorReporter);

    }

    @After public void tearDown() {
        server.close();
        sender.close();
    }

    @Test public void sendMessageTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121l, "WARNING");
        boolean success = sender.sendMessage(gelfMessage);
        assertTrue(success);
        verifyZeroInteractions(errorReporter);
        List<Object> jsonValues = server.getJsonValues();
        assertEquals(1, jsonValues.size());
        JSONObject messageJson = (JSONObject) jsonValues.get(0);
        assertEquals(gelfMessage.getShortMessage(), messageJson.get("short_message"));
        assertEquals(gelfMessage.getFullMessage(), messageJson.get("full_message"));
        assertEquals(gelfMessage.getTimestamp(), messageJson.get("timestamp"));
        assertEquals(gelfMessage.getLevel(), messageJson.get("level"));

    }

    @Test public void sendMessageFailureTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        String uri = "http://127.0.0.1:19393";
        GelfHTTPSender sender = new GelfHTTPSender(new URL(uri), errorReporter);
        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121l, "WARNING");
        boolean success = sender.sendMessage(gelfMessage);
        assertFalse(success);
        verify(errorReporter, times(1)).reportError(anyString(), any(Exception.class));
    }

    @Test public void testWithLoggingContext() throws JoranException {
        LoggerContext lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        URL xmlConfigFile = getClass().getResource("/logback-gelf-with-http.xml");
        configurator.doConfigure(xmlConfigFile);
        Logger testLogger = lc.getLogger("testLogger");
        testLogger.error("Hi there");
        List<Object> jsonValues = server.getJsonValues();
        String uri = server.getHandlerInitializer().getHandler().getUri();
        assertEquals("/foo/bar", uri);
        assertEquals(1, jsonValues.size());
        JSONObject jsonObject = (JSONObject) jsonValues.get(0);
        assertEquals("Hi there", jsonObject.get("short_message"));

    }

}
