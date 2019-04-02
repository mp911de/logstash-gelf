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
 * @since 27.09.13 07:47
 */
class GelfLogAppenderPropertiesDynamicMdcTest {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MDC_MY_MDC = "myMdc";
    private static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    private static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String VALUE_3 = "value3";
    private static final String SOME_FIELD = "someField";
    private static final String SOME_OTHER_FIELD = "someOtherField";

    @BeforeEach
    void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        PropertyConfigurator.configure(getClass().getResource("/log4j/log4j-test-with-mdcfields.properties"));

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

        assertThat(gelfMessage.getField(MDC_MY_MDC)).isNull();
    }

    @Test
    void testWithMdcPrefix() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(MDC_MY_MDC, VALUE_1);
        MDC.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        MDC.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC)).isEqualTo(VALUE_1);
        assertThat(gelfMessage.getField(MY_MDC_WITH_SUFFIX1)).isEqualTo(VALUE_2);
        assertThat(gelfMessage.getField(MY_MDC_WITH_SUFFIX2)).isEqualTo(VALUE_3);

    }

    @Test
    void testWithMdcRegex() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(SOME_FIELD, "included");
        MDC.put(SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(SOME_FIELD)).isEqualTo("included");
        assertThat(gelfMessage.getField(SOME_OTHER_FIELD)).isNull();
    }
}
