package biz.paluch.logging.gelf.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

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
 * @author Mark Paluch
 * @since 16.07.14 17:30
 */
public class GelfLogAppenderPropertiesIncludeFullMdcTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String MDC_MY_MDC1 = "aMdcValue";
    public static final String MDC_MY_MDC2 = "differentMdc";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";

    @Before
    public void before() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        PropertyConfigurator.configure(getClass().getResource("/log4j/log4j-test-with-includefullmdc.properties"));

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

        assertNull(gelfMessage.getField(MDC_MY_MDC1));
        assertNull(gelfMessage.getField(MDC_MY_MDC2));
    }

    @Test
    public void testWithMdc() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        MDC.put(MDC_MY_MDC1, VALUE_1);
        MDC.put(MDC_MY_MDC2, VALUE_2);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(VALUE_1, gelfMessage.getField(MDC_MY_MDC1));
        assertEquals(VALUE_2, gelfMessage.getField(MDC_MY_MDC2));

    }

    @Test
    public void testEmptyMessage() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        logger.info("");
        assertEquals(0, GelfTestSender.getMessages().size());

    }

}
