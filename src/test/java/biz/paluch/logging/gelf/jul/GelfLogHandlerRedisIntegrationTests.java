package biz.paluch.logging.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.MDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.Sockets;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.sender.RedisGelfSenderProvider;
import biz.paluch.logging.gelf.standalone.DefaultGelfSenderConfiguration;
import redis.clients.jedis.Jedis;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerRedisIntegrationTests {

    private Jedis jedis;

    @BeforeEach
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

        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-redis-logging.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);

        List<String> list = jedis.lrange("list", 0, jedis.llen("list"));
        assertThat(list).hasSize(1);

        Map<String, Object> map = JsonUtil.parseToMap(list.get(0));

        assertThat(map.get("full_message")).isEqualTo(expectedMessage);
        assertThat(map.get("short_message")).isEqualTo(expectedMessage);
        assertThat(map.get("fieldName1")).isEqualTo("fieldValue1");

    }

    @Test
    public void testSentinel() throws Exception {

        assumeTrue(Sockets.isOpen("localhost", 26379));

        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-redis-sentinel-logging.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);

        List<String> list = jedis.lrange("list", 0, jedis.llen("list"));
        assertThat(list).hasSize(1);

        Map<String, Object> map = JsonUtil.parseToMap(list.get(0));

        assertThat(map.get("full_message")).isEqualTo(expectedMessage);
        assertThat(map.get("short_message")).isEqualTo(expectedMessage);
        assertThat(map.get("fieldName1")).isEqualTo("fieldValue1");

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

    @Test
    public void uriWithoutHost() throws Exception {

        String uri = "redis:///#list";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        assertThrows(IllegalArgumentException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                new RedisGelfSenderProvider().create(configuration);
            }
        });

    }

    @Test
    public void uriWithoutFragment() throws Exception {

        String uri = "redis://host/";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        assertThrows(IllegalArgumentException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                new RedisGelfSenderProvider().create(configuration);
            }
        });
    }

    @Test
    public void uriWithoutFragment2() throws Exception {

        String uri = "redis://host";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        assertThrows(IllegalArgumentException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                new RedisGelfSenderProvider().create(configuration);
            }
        });
    }

    @Test
    public void uriWithoutFragment3() throws Exception {

        String uri = "redis://host#";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        assertThrows(IllegalArgumentException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                new RedisGelfSenderProvider().create(configuration);
            }
        });
    }

    @Test
    public void testRedisNotAvailable() throws Exception {

        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-redis-not-available.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);
    }
}
