package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Duration;
import com.google.code.tempusfugit.temporal.Timeout;
import com.google.code.tempusfugit.temporal.WaitFor;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.netty.NettyLocalServer;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderNettyUdpIntegrationTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    private static LoggerContext loggerContext;
    private static NettyLocalServer server = new NettyLocalServer(NioDatagramChannel.class);

    @BeforeClass
    public static void setupClass() throws Exception {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-netty-udp.xml");
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
        server.run();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        loggerContext.reconfigure();
        server.close();
    }

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
        server.clear();

    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);

        waitForGelf();

        List jsonValues = server.getJsonValues();
        assertEquals(1, jsonValues.size());

        Map<String, Object> jsonValue = (Map<String, Object>) jsonValues.get(0);

        assertEquals(RuntimeContainer.FQDN_HOSTNAME, jsonValue.get(GelfMessage.FIELD_HOST));
        assertEquals(RuntimeContainer.HOSTNAME, jsonValue.get("_server.simple"));
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, jsonValue.get("_server.fqdn"));
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, jsonValue.get("_server"));
        assertEquals(RuntimeContainer.ADDRESS, jsonValue.get("_server.addr"));

        assertEquals(getClass().getName(), jsonValue.get("_className"));
        assertEquals(getClass().getSimpleName(), jsonValue.get("_simpleClassName"));

        assertEquals(EXPECTED_LOG_MESSAGE, jsonValue.get(GelfMessage.FIELD_FULL_MESSAGE));
        assertEquals(EXPECTED_LOG_MESSAGE, jsonValue.get(GelfMessage.FIELD_SHORT_MESSAGE));

        assertEquals("INFO", jsonValue.get("_level"));
        assertEquals("6", jsonValue.get(GelfMessage.FIELD_LEVEL));

        assertEquals("logstash-gelf", jsonValue.get(GelfMessage.FIELD_FACILITY));
        assertEquals("fieldValue1", jsonValue.get("_fieldName1"));
        assertEquals("fieldValue2", jsonValue.get("_fieldName2"));
        assertEquals(GelfMessage.DEFAULT_FACILITY, jsonValue.get("facility"));

    }

    @Test(expected = TimeoutException.class)
    public void testEmptyMessage() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("");

        waitForGelf();

    }

    private void waitForGelf() throws InterruptedException, TimeoutException {
        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return !server.getJsonValues().isEmpty();
            }
        }, Timeout.timeout(Duration.seconds(2)));
    }

    @Test
    public void testVeryLargeMessage() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            int charId = (int) (Math.random() * Character.MAX_CODE_POINT);
            builder.append(charId);
        }
        logger.info(builder.toString());
        waitForGelf();

        List jsonValues = server.getJsonValues();
        assertEquals(1, jsonValues.size());

        Map<String, Object> jsonValue = (Map<String, Object>) jsonValues.get(0);

        assertEquals(RuntimeContainer.ADDRESS, jsonValue.get("_server.addr"));

        assertEquals(getClass().getSimpleName(), jsonValue.get("_simpleClassName"));

        String shortMessage = builder.substring(0, 249);
        assertEquals(builder.toString(), jsonValue.get("full_message"));
        assertEquals(shortMessage, jsonValue.get("short_message"));
    }
}
