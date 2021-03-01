package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderSystemPropertiesTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String PROPERTY1 = "myproperty";
    private static final String PROPERTY1_VALUE = "value of myproperty";

    private static final String PROPERTY2 = "otherproperty";
    private static final String PROPERTY2_VALUE = "value of otherproperty";
    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-systemproperties.xml");
        PropertiesUtil.getProperties().reload();
        loggerContext = (LoggerContext) LogManager.getContext(false);

    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        PropertiesUtil.getProperties().reload();
        loggerContext.reconfigure();
    }

    @BeforeEach
    void before() throws Exception {
        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        setup();
    }

    void setup() {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
        PropertiesUtil.getProperties().reload();
        loggerContext.reconfigure();
    }

    @Test
    void testDefaults() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("propertyField1")).isEqualTo(System.getProperty("user.language"));
        assertThat(gelfMessage.getField("propertyField2")).isEqualTo("${sys:myproperty}");
        assertThat(gelfMessage.getField("propertyField3")).isEqualTo("${sys:otherproperty:fallback}");
        assertThat(gelfMessage.getField("propertyField4")).isEqualTo("embedded${sys:myproperty}property");
    }

    @Test
    void testAfterSetProperties() throws Exception {

        System.setProperty(PROPERTY1, PROPERTY1_VALUE);
        System.setProperty(PROPERTY2, PROPERTY2_VALUE);

        setup();
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("propertyField1")).isEqualTo(System.getProperty("user.language"));
        assertThat(gelfMessage.getField("propertyField2")).isEqualTo(PROPERTY1_VALUE);
        assertThat(gelfMessage.getField("propertyField3")).isEqualTo("${sys:otherproperty:fallback}");
        assertThat(gelfMessage.getField("propertyField4")).isEqualTo("embedded" + PROPERTY1_VALUE + "property");
    }

}
