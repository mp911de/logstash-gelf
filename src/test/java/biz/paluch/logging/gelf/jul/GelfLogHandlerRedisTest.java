package biz.paluch.logging.gelf.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.Sockets;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.sender.RedisGelfSenderProvider;
import biz.paluch.logging.gelf.standalone.DefaultGelfSenderConfiguration;
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
        assumeTrue(Sockets.isOpen("localhost", 6479));

        GelfTestSender.getMessages().clear();
        MDC.remove("mdcField1");

        jedis = new Jedis("localhost", 6479);
        jedis.flushDB();
        jedis.flushAll();
    }

    @Test
    public void testStandalone() throws Exception {
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/test-redis-logging.properties"));

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

    @Test
    public void testSentinel() throws Exception {
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/test-redis-sentinel-logging.properties"));

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

    @Test
    public void testMinimalRedisUri() throws Exception {
        assumeTrue(Sockets.isOpen("localhost", 6379));
        String uri = "redis://localhost/#list";

        RedisGelfSenderProvider provider = new RedisGelfSenderProvider();
        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        GelfSender gelfSender = provider.create(configuration);

        gelfSender.sendMessage(new GelfMessage());
    }

    @Test
    public void testRedisWithPortUri() throws Exception {
        String uri = "redis://localhost:6479/#list";

        RedisGelfSenderProvider provider = new RedisGelfSenderProvider();
        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        GelfSender gelfSender = provider.create(configuration);

        gelfSender.sendMessage(new GelfMessage());
        gelfSender.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void uriWithoutHost() throws Exception {
        String uri = "redis:///#list";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        new RedisGelfSenderProvider().create(configuration);

    }

    @Test(expected = IllegalArgumentException.class)
    public void uriWithoutFragment() throws Exception {
        String uri = "redis://host/";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        new RedisGelfSenderProvider().create(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void uriWithoutFragment2() throws Exception {
        String uri = "redis://host";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        new RedisGelfSenderProvider().create(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void uriWithoutFragment3() throws Exception {
        String uri = "redis://host#";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        new RedisGelfSenderProvider().create(configuration);
    }



}
