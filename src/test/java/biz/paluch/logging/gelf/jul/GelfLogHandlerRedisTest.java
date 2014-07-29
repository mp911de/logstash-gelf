package biz.paluch.logging.gelf.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import biz.paluch.logging.gelf.GelfTestSender;
import org.apache.log4j.MDC;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerRedisTest {
    private Jedis jedis;

    @Before
    public void before() throws Exception {
        // enable the test with -Dtest.withRedis=true
        assumeTrue(Boolean.getBoolean("test.withRedis"));

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/test-redis-logging.properties"));
        MDC.remove("mdcField1");

        jedis = new Jedis("localhost", 6479);
        jedis.flushDB();
        jedis.flushAll();
    }

    @Test
    public void testWithoutResourceBundle() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);

        List<String> list = jedis.lrange("list", 0, jedis.llen("list"));
        assertEquals(1, list.size());

        Map<String, String> map = (Map<String, String>) JSONValue.parse(list.get(0));

        assertEquals(expectedMessage, map.get("full_message"));
        assertEquals(expectedMessage, map.get("short_message"));
        assertEquals("fieldValue1", map.get("fieldName1"));

    }

}
