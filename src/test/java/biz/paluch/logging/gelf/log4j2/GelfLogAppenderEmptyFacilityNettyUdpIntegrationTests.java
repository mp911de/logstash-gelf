package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.netty.NettyLocalServer;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderEmptyFacilityNettyUdpIntegrationTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    private static LoggerContext loggerContext;
    private static NettyLocalServer server = new NettyLocalServer(NioDatagramChannel.class);

    @BeforeClass
    public static void setupClass() throws Exception {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-empty-facility-netty-udp.xml");
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

        assertNull(jsonValue.get("facility"));

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

        String shortMessage = builder.substring(0, 249);
        assertEquals(builder.toString(), jsonValue.get("full_message"));
        assertEquals(shortMessage, jsonValue.get("short_message"));

    }

}
