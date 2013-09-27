package biz.paluch.logging.gelf.jboss7;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.log4j.GelfTestSender;
import org.apache.log4j.MDC;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 08:36
 */
public class JBoss7GelfLogHandlerTest {
    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();

        LogManager.getLogManager().reset();

        MDC.clear();
    }

    @Test
    public void testSimple() throws Exception {

        JBoss7GelfLogHandler handler = getjBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("Blubb Test", gelfMessage.getFullMessage());
        assertEquals("6", gelfMessage.getLevel());
        assertEquals("Blubb Test", gelfMessage.getShortMessage());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());

    }

    @Test
    public void testFields() throws Exception {

        JBoss7GelfLogHandler handler = getjBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put("mdcField1", "a value");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("fieldValue1", gelfMessage.getField("fieldName1"));
        assertEquals("fieldValue2", gelfMessage.getField("fieldName2"));
        assertEquals("a value", gelfMessage.getField("mdcField1"));
        assertNull(gelfMessage.getField("mdcField2"));

    }

    private JBoss7GelfLogHandler getjBoss7GelfLogHandler() {
        JBoss7GelfLogHandler handler = new JBoss7GelfLogHandler();

        handler.setGraylogHost("udp:localhost");
        handler.setGraylogPort(12202);
        handler.setFacility("java-test");
        handler.setExtractStackTrace(true);
        handler.setFilterStackTrace(true);
        handler.setTimestampPattern("yyyy-MM-dd HH:mm:ss,SSSS");
        handler.setMaximumMessageSize(8192);
        handler.setAdditionalFields("fieldName1=fieldValue1,fieldName2=fieldValue2");
        handler.setTestSenderClass("biz.paluch.logging.gelf.log4j.GelfTestSender");
        handler.setLevel(Level.INFO);
        handler.setMdcFields("mdcField1,mdcField2");
        return handler;
    }
}
