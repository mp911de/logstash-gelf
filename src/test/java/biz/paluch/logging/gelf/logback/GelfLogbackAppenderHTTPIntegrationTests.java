package biz.paluch.logging.gelf.logback;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import biz.paluch.logging.gelf.netty.NettyLocalHTTPServer;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author Mark Paluch
 */
public class GelfLogbackAppenderHTTPIntegrationTests {

    private NettyLocalHTTPServer server;

    LoggerContext lc = null;

    @Before
    public void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-http.xml");

        configurator.doConfigure(xmlConfigFile);

        server = new NettyLocalHTTPServer();
        server.run();

        MDC.remove("mdcField1");
    }

    @After
    public void after() throws Exception {
        server.close();
    }

    @Test
    public void testHttpSender() {

        Logger testLogger = lc.getLogger("testLogger");

        testLogger.error("Hi there");

        List<Object> jsonValues = server.getJsonValues();
        String uri = server.getHandlerInitializer().getHandler().getUri();

        assertEquals("/foo/bar", uri);
        assertEquals(1, jsonValues.size());

        Map<String, Object> jsonObject = (Map<String, Object>) jsonValues.get(0);
        assertEquals("Hi there", jsonObject.get("short_message"));
    }
}
