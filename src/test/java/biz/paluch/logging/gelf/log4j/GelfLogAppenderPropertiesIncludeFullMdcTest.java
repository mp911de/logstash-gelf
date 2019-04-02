package biz.paluch.logging.gelf.log4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 16.07.14 17:30
 */
class GelfLogAppenderPropertiesIncludeFullMdcTest {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MDC_MY_MDC1 = "aMdcValue";
    private static final String MDC_MY_MDC2 = "differentMdc";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";

    @BeforeEach
    void before() throws Exception {

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
    void testWithoutFields() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC1)).isNull();
        assertThat(gelfMessage.getField(MDC_MY_MDC2)).isNull();
    }

    @Test
    void testWithMdc() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        MDC.put(MDC_MY_MDC1, VALUE_1);
        MDC.put(MDC_MY_MDC2, VALUE_2);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC1)).isEqualTo(VALUE_1);
        assertThat(gelfMessage.getField(MDC_MY_MDC2)).isEqualTo(VALUE_2);
    }

    @Test
    void testEmptyMessage() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        logger.info("");
        assertThat(GelfTestSender.getMessages()).isEmpty();
    }
}
