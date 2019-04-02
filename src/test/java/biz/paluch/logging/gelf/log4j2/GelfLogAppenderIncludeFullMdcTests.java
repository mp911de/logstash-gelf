package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderIncludeFullMdcTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MDC_MY_MDC1 = "aMdcValue";
    private static final String MDC_MY_MDC2 = "differentMdc";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-include-full-mdc.xml");
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        loggerContext.reconfigure();
    }

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    @Test
    void testWithoutFields() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC1)).isNull();
        assertThat(gelfMessage.getField(MDC_MY_MDC2)).isNull();
    }

    @Test
    void testWithMdc() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(MDC_MY_MDC1, VALUE_1);
        ThreadContext.put(MDC_MY_MDC2, VALUE_2);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC1)).isEqualTo(VALUE_1);
        assertThat(gelfMessage.getField(MDC_MY_MDC2)).isEqualTo(VALUE_2);

    }

}
