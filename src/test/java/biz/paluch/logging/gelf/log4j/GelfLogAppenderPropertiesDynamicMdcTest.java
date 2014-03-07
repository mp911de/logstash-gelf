package biz.paluch.logging.gelf.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 07:47
 */
public class GelfLogAppenderPropertiesDynamicMdcTest {

    public static final String LOG_MESSAGE = "foo bar test log message";

    @Before
    public void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        PropertyConfigurator.configure(getClass().getResource("/log4j-test-with-mdcfields.properties"));

        if (MDC.getContext() != null && MDC.getContext().keySet() != null) {

            Set<String> keys = new HashSet<String>(MDC.getContext().keySet());

            for (String key : keys) {
                MDC.remove(key);
            }
        }
    }

    @Test
    public void testWithoutFields() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField("myMdc");
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        MDC.put("myMdc", "value");
        MDC.put("myMdc-with-suffix1", "value1");
        MDC.put("myMdc-with-suffix2", "value2");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("value", gelfMessage.getField("myMdc"));
        assertEquals("value1", gelfMessage.getField("myMdc-with-suffix1"));
        assertEquals("value2", gelfMessage.getField("myMdc-with-suffix2"));

    }

    @Test
    public void testWithMdcRegex() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        MDC.put("someField", "included");
        MDC.put("someOtherField", "excluded");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField("someField"));
        assertNull(gelfMessage.getField("someOtherField"));

    }
}
