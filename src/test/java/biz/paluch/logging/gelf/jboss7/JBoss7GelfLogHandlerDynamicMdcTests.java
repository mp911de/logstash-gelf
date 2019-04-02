package biz.paluch.logging.gelf.jboss7;

import static biz.paluch.logging.gelf.jboss7.JBoss7LogTestUtil.getJBoss7GelfLogHandler;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.logmanager.MDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:36
 */
class JBoss7GelfLogHandlerDynamicMdcTests {
    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    private static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    private static final String MDC_SOME_FIELD = "someField";
    private static final String MDC_SOME_OTHER_FIELD = "someOtherField";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String VALUE_3 = "value3";
    private static final String MDC_MY_MDC = "myMdc";

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
        MDC.clear();
    }

    @Test
    void testWithoutFields() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        assertThat(myMdc).isNull();
    }

    @Test
    void testWithMdcPrefix() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
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

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put(MDC_SOME_FIELD, "included");
        MDC.put(MDC_SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_SOME_FIELD)).isEqualTo("included");
        assertThat(gelfMessage.getField("someOtherField")).isNull();

    }

    @Test
    void testWithDifferentMDCsPrefix() throws Exception {

        JBoss7GelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

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

    private JBoss7GelfLogHandler getLogHandler() {
        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();
        handler.setDynamicMdcFields("myMdc.*,[a-z]+Field");

        return handler;
    }
}
