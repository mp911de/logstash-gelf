package biz.paluch.logging.gelf.log4j;

import biz.paluch.logging.gelf.intern.GelfMessage;
import org.apache.log4j.MDC;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerTest {
    @Before
    public void before() throws Exception {

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/test-logging.properties"));
        MDC.remove("mdcField1");
    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = Logger.getLogger(getClass().getName());

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("Blubb Test", gelfMessage.getFullMessage());
        assertEquals("6", gelfMessage.getLevel());
        assertEquals("Blubb Test", gelfMessage.getShortMessage());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());

    }
}
