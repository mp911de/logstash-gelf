package biz.paluch.logging.gelf.logback;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-07
 */
public class GelfLogbackAppenderDynamicMdcTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String MDC_MY_MDC = "myMdc";
    public static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    public static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";
    public static final String VALUE_3 = "value3";
    public static final String SOME_FIELD = "someField";
    public static final String SOME_OTHER_FIELD = "someOtherField";
    LoggerContext lc = null;

    @BeforeEach
    public void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-dynamic-fields.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.clear();
    }

    @Test
    public void testWithoutFields() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        assertThat(myMdc).isNull();
    }

    @Test
    public void testWithNullValue() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put(MDC_MY_MDC, VALUE_1);
        MDC.put(MY_MDC_WITH_SUFFIX1, null);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(MDC_MY_MDC)).isEqualTo(VALUE_1);
        assertThat(gelfMessage.getAdditonalFields()).doesNotContainKey(MY_MDC_WITH_SUFFIX1);
        assertThat(gelfMessage.getAdditonalFields()).doesNotContainKey(MY_MDC_WITH_SUFFIX2);

    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        Logger logger = lc.getLogger(getClass());
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
    public void testWithMdcRegex() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put(SOME_FIELD, "included");
        MDC.put(SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(SOME_FIELD)).isEqualTo("included");
        assertThat(gelfMessage.getField(SOME_OTHER_FIELD)).isNull();
    }

    @Test
    public void testWithMdcFieldTypes() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put("myMdcs", "String");
        MDC.put("myMdcl", "1");
        MDC.put("myMdci", "2");
        MDC.put("myMdcd", "2.1");
        MDC.put("myMdcf", "2.2");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        Map<String, Object> jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcs")).isEqualTo("String");
        assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        assertThat(jsonObject.get("myMdcd")).isEqualTo(2.1);
        assertThat(jsonObject.get("myMdcf")).isEqualTo(2.2);

        MDC.put("myMdcl", "1.1");
        MDC.put("myMdci", "2.1");
        MDC.put("myMdcd", "wrong");
        MDC.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        assertThat(jsonObject.get("myMdcd")).isNull();
        assertThat(jsonObject.get("myMdcf")).isEqualTo(0.0);

        MDC.put("myMdcl", "b");
        MDC.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcl")).isNull();
        assertThat(jsonObject.get("myMdci")).isEqualTo(0);
    }
}
