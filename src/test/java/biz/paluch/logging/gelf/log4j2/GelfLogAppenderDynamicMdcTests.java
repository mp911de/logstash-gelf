package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderDynamicMdcTests {
    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MDC_MY_MDC = "myMdc";
    private static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    private static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    private static final String VALUE_1 = "value";
    private static final String VALUE_2 = "value1";
    private static final String VALUE_3 = "value2";
    private static final String SOME_FIELD = "someField";
    private static final String SOME_OTHER_FIELD = "someOtherField";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-dynamic-mdc.xml");
        PropertiesUtil.getProperties().reload();
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        PropertiesUtil.getProperties().reload();
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

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        assertThat(myMdc).isNull();
    }

    @Test
    void testWithMdcPrefix() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(MDC_MY_MDC, VALUE_1);
        ThreadContext.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        ThreadContext.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC)).isEqualTo(VALUE_1);
        assertThat(gelfMessage.getField(MY_MDC_WITH_SUFFIX1)).isEqualTo(VALUE_2);
        assertThat(gelfMessage.getField(MY_MDC_WITH_SUFFIX2)).isEqualTo(VALUE_3);
    }

    @Test
    void testWithMdcRegex() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(SOME_FIELD, "included");
        ThreadContext.put(SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(SOME_FIELD)).isEqualTo("included");
        assertThat(gelfMessage.getField(SOME_OTHER_FIELD)).isNull();
    }

    @Test
    void testWithMdcFieldTypes() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put("myMdcs", "String");
        ThreadContext.put("myMdcl", "1");
        ThreadContext.put("myMdci", "2");
        ThreadContext.put("myMdcd", "2.1");
        ThreadContext.put("myMdcf", "2.2");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        Map<String, Object> jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcs")).isEqualTo("String");
        assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        assertThat(jsonObject.get("myMdcd")).isEqualTo(2.1);
        assertThat(jsonObject.get("myMdcf")).isEqualTo(2.2);

        ThreadContext.put("myMdcl", "1.1");
        ThreadContext.put("myMdci", "2.1");
        ThreadContext.put("myMdcd", "wrong");
        ThreadContext.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        assertThat(jsonObject.get("myMdcd")).isNull();
        assertThat(jsonObject.get("myMdcf")).isEqualTo(0.0);

        ThreadContext.put("myMdcl", "b");
        ThreadContext.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcl")).isNull();
        assertThat(jsonObject.get("myMdci")).isEqualTo(0);
    }
}
