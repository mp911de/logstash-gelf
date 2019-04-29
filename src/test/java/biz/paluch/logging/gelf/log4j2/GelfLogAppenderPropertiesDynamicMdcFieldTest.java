package biz.paluch.logging.gelf.log4j2;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thomas Herzog
 * @since 29.04.19 18:00
 */
class GelfLogAppenderPropertiesDynamicMdcFieldTest {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MY_MDC_LONG_VALUE_1 = "myMdc.longValue1";
    private static final String MY_MDC_LONG_VALUE_2 = "myMdc.longValue2";
    private static final String MY_MDC_DOUBLE_VALUE_1 = "myMdc.doubleValue1";
    private static final String MY_MDC_DOUBLE_VALUE_2 = "myMdc.doubleValue2";
    private static final String MY_MDC_STRING_VALUE = "myMdc.stringValue";
    private static final String MY_MDC_UNDEFINED_VALUE = "myMdc.undefinedValue";
    private static final Long LONG_VALUE_1 = 1L;
    private static final Long LONG_VALUE_2 = 2L;
    private static final Double DOUBLE_VALUE_1 = 1.0;
    private static final Double DOUBLE_VALUE_2 = 2.0;
    private static final String STRING_VALUE = "1.0";
    private static final String UNDEFINED_VALUE = "v1.0";

    private LoggerContext loggerContext;

    @BeforeEach
    private void beforeEach() throws Exception {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-dynamic-mdcfieltypes.xml");
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @Test
    void testWithRegexMatch() throws Exception {
        // -- Given --
        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(MY_MDC_LONG_VALUE_1, LONG_VALUE_1.toString());
        ThreadContext.put(MY_MDC_LONG_VALUE_2, LONG_VALUE_2.toString());
        ThreadContext.put(MY_MDC_DOUBLE_VALUE_1, DOUBLE_VALUE_1.toString());
        ThreadContext.put(MY_MDC_DOUBLE_VALUE_2, DOUBLE_VALUE_2.toString());
        ThreadContext.put(MY_MDC_STRING_VALUE, STRING_VALUE);
        ThreadContext.put(MY_MDC_UNDEFINED_VALUE, UNDEFINED_VALUE);

        // -- When --
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);
        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        // -- Then --
        final String json = gelfMessage.toJson();
        HashMap<String, Object> result = new ObjectMapper()
                .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
                .readValue(json, HashMap.class);
        assertThat(result.get("_" + MY_MDC_LONG_VALUE_1)).isNotNull().isEqualTo(LONG_VALUE_1);
        assertThat(result.get("_" + MY_MDC_LONG_VALUE_2)).isNotNull().isEqualTo(LONG_VALUE_2);
        assertThat(result.get("_" + MY_MDC_DOUBLE_VALUE_1)).isNotNull().isEqualTo(DOUBLE_VALUE_1);
        assertThat(result.get("_" + MY_MDC_DOUBLE_VALUE_2)).isNotNull().isEqualTo(DOUBLE_VALUE_2);
        assertThat(result.get("_" + MY_MDC_STRING_VALUE)).isNotNull().isEqualTo(STRING_VALUE);
        assertThat(result.get("_" + MY_MDC_UNDEFINED_VALUE)).isNotNull().isEqualTo(UNDEFINED_VALUE);
    }

    @Test
    void testWithInvalidType() throws Exception {
        // -- Given --
        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(MY_MDC_LONG_VALUE_1, "v1.0");

        // -- When --
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);
        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        // -- Then --
        final String json = gelfMessage.toJson();
        HashMap<String, Object> result = new ObjectMapper()
                .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
                .readValue(json, HashMap.class);
        assertThat(result.get("_" + MY_MDC_LONG_VALUE_1)).isNull();
    }
}
