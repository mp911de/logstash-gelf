package biz.paluch.logging.gelf.jboss7;

import static biz.paluch.logging.gelf.jboss7.JBoss7LogTestUtil.getJBoss7GelfLogHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.logmanager.MDC;
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
    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    public static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    public static final String MDC_SOME_FIELD = "someField";
    public static final String MDC_SOME_OTHER_FIELD = "someOtherField";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";
    public static final String VALUE_3 = "value3";
    public static final String MDC_MY_MDC = "myMdc";

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
        MDC.clear();
    }

    @Test
    public void testWithoutFields() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put(MDC_MY_MDC, VALUE_1);
        MDC.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        MDC.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(VALUE_1, gelfMessage.getField(MDC_MY_MDC));
        assertEquals(VALUE_2, gelfMessage.getField(MY_MDC_WITH_SUFFIX1));
        assertEquals(VALUE_3, gelfMessage.getField(MY_MDC_WITH_SUFFIX2));

    }

    @Test
    public void testWithMdcRegex() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put(MDC_SOME_FIELD, "included");
        MDC.put(MDC_SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField(MDC_SOME_FIELD));
        assertNull(gelfMessage.getField("someOtherField"));

    }

    @Test
    public void testWithDifferentMDCsPrefix() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put(MDC_MY_MDC, VALUE_1);
        MDC.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        MDC.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(VALUE_1, gelfMessage.getField(MDC_MY_MDC));
        assertEquals(VALUE_2, gelfMessage.getField(MY_MDC_WITH_SUFFIX1));
        assertEquals(VALUE_3, gelfMessage.getField(MY_MDC_WITH_SUFFIX2));

    }

    private JBoss7GelfLogHandler getLogHandler() {
        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();
        handler.setDynamicMdcFields("myMdc.*,[a-z]+Field");

        return handler;
    }
}
