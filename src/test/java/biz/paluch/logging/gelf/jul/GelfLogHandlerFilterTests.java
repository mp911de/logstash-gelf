package biz.paluch.logging.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.MDC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerFilterTests {

    @BeforeEach
    public void before() throws Exception {

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-logging-with-filter.properties"));
        MDC.remove("mdcField1");
    }

    @AfterEach
    public void after() throws Exception {
        LogManager.getLogManager().reset();

    }

    @Test
    public void testSimpleInfo() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.info(expectedMessage);
        assertThat(GelfTestSender.getMessages()).isEmpty();
    }
}
