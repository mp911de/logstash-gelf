package biz.paluch.logging.gelf.log4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.tempusfugit.temporal.*;

import biz.paluch.logging.gelf.netty.NettyLocalServer;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderNettyIntegrationTests {

    private static NettyLocalServer server = new NettyLocalServer(NioDatagramChannel.class);

    @BeforeClass
    public static void setupClass() throws Exception {
        server.run();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.close();
    }

    @Before
    public void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        DOMConfigurator.configure(getClass().getResource("/log4j/log4j-netty-warn.xml"));
        server.clear();
    }

    private void waitForGelf(final int minMessageCount) throws InterruptedException, TimeoutException {
        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return server.getJsonValues().size() >= minMessageCount;
            }
        }, Timeout.timeout(Duration.seconds(2)));
    }

    @Test
    public void testLogInfoLimitedCategory() throws Exception {
        Logger logger = Logger.getLogger("org.something.else");
        logger.info("info1");
        logger.info("info2");
        logger.info("info3");
        logger.info("info4");

        new ThreadSleep(Duration.seconds(2)).sleep();

        assertThat(server.getJsonValues()).isEmpty();
    }

    @Test
    public void testLogWarnLimitedCategory() throws Exception {
        Logger logger = Logger.getLogger("org.something.else");
        logger.warn("warn1");
        logger.warn("warn2");
        logger.warn("warn3");
        logger.warn("warn4");

        waitForGelf(4);

        assertThat(server.getJsonValues()).hasSize(4);
    }

    @Test
    public void testLogInfo() throws Exception {
        Logger logger = Logger.getLogger("mylog");
        logger.info("info1");
        logger.info("info2");
        logger.info("info3");
        logger.info("info4");

        new ThreadSleep(Duration.seconds(2)).sleep();

        assertThat(server.getJsonValues()).isEmpty();
    }

    @Test
    public void testLogWarn() throws Exception {
        Logger logger = Logger.getLogger("mylog");
        logger.warn("warn1");
        logger.warn("warn2");
        logger.warn("warn3");
        logger.warn("warn4");

        waitForGelf(4);

        assertThat(server.getJsonValues()).hasSize(4);
    }
}
