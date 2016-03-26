package biz.paluch.logging.gelf.jul;

import static org.junit.Assert.assertEquals;
import biz.paluch.logging.gelf.GelfTestSender;
import org.apache.log4j.MDC;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerFilterTest {
    @Before
    public void before() throws Exception {

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-logging-with-filter.properties"));
        MDC.remove("mdcField1");
    }

    @After
    public void after() throws Exception {
        LogManager.getLogManager().reset();

    }

    @Test
    public void testSimpleInfo() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.info(expectedMessage);
        assertEquals(0, GelfTestSender.getMessages().size());
    }
}
