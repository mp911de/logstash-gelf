package biz.paluch.logging.gelf.jboss7;

import static biz.paluch.logging.gelf.jboss7.JBoss7LogTestUtil.getJBoss7GelfLogHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.MDC;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 08:36
 */
public class JBoss7GelfLogHandlerDynamicMdcTest
{

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();

        JBoss7GelfLogHandler handler = getLogHandler();
        LogManager.getLogManager().reset();

        if (MDC.getContext() != null && MDC.getContext().keySet() != null) {

            Set<String> keys = new HashSet<String>(MDC.getContext().keySet());

            for (String key : keys) {
                MDC.remove(key);
            }
        }
        org.slf4j.MDC.clear();
    }

    @Test
    public void testWithoutFields() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField("myMdc");
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put("myMdc", "value");
        MDC.put("myMdc-with-suffix1", "value1");
        MDC.put("myMdc-with-suffix2", "value2");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("value", gelfMessage.getField("myMdc"));
        assertEquals("value1", gelfMessage.getField("myMdc-with-suffix1"));
        assertEquals("value2", gelfMessage.getField("myMdc-with-suffix2"));

    }

    @Test
    public void testWithMdcRegex() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put("someField", "included");
        MDC.put("someOtherField", "excluded");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField("someField"));
        assertNull(gelfMessage.getField("someOtherField"));

    }

    @Test
    public void testWithDifferentMDCsPrefix() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        org.apache.log4j.MDC.put("myMdc", "value");
        org.slf4j.MDC.put("myMdc-with-suffix1", "value1");
        org.slf4j.MDC.put("myMdc-with-suffix2", "value2");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("value", gelfMessage.getField("myMdc"));
        assertEquals("value1", gelfMessage.getField("myMdc-with-suffix1"));
        assertEquals("value2", gelfMessage.getField("myMdc-with-suffix2"));

    }

    private JBoss7GelfLogHandler getLogHandler() {
        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();
        handler.setDynamicMdcFields("myMdc.*,[a-z]+Field");

        return handler;
    }
}
