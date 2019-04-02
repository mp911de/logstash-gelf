package biz.paluch.logging.gelf.logback;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

class GelfLogbackAppenderSystemPropertiesTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String PROPERTY1 = "myproperty";
    private static final String PROPERTY1_VALUE = "value of myproperty";

    private static final String PROPERTY2 = "otherproperty";
    private static final String PROPERTY2_VALUE = "value of otherproperty";

    private LoggerContext lc = null;

    @BeforeEach
    void before() throws Exception {

        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        setup();

    }

    void setup() throws JoranException {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-systemproperties-fields.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();
    }

    @Test
    void testDefaults() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("propertyField1")).isEqualTo(System.getProperty("user.language"));
        assertThat(gelfMessage.getField("propertyField2")).isEqualTo("myproperty_IS_UNDEFINED");
        assertThat(gelfMessage.getField("propertyField3")).isEqualTo("otherproperty:fallback_IS_UNDEFINED");
        assertThat(gelfMessage.getField("propertyField4")).isEqualTo("embeddedmyproperty_IS_UNDEFINEDproperty");
    }

    @Test
    void testAfterSetProperties() throws Exception {

        System.setProperty(PROPERTY1, PROPERTY1_VALUE);
        System.setProperty(PROPERTY2, PROPERTY2_VALUE);

        setup();
        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("propertyField1")).isEqualTo(System.getProperty("user.language"));
        assertThat(gelfMessage.getField("propertyField2")).isEqualTo(PROPERTY1_VALUE);
        assertThat(gelfMessage.getField("propertyField3")).isEqualTo("otherproperty:fallback_IS_UNDEFINED");
        assertThat(gelfMessage.getField("propertyField4")).isEqualTo("embedded" + PROPERTY1_VALUE + "property");
    }

}
