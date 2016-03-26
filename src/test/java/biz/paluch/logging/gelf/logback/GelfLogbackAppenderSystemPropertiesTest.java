package biz.paluch.logging.gelf.logback;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class GelfLogbackAppenderSystemPropertiesTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String PROPERTY1 = "myproperty";
    public static final String PROPERTY1_VALUE = "value of myproperty";

    public static final String PROPERTY2 = "otherproperty";
    public static final String PROPERTY2_VALUE = "value of otherproperty";

    private LoggerContext lc = null;

    @Before
    public void before() throws Exception {

        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        setup();

    }

    protected void setup() throws JoranException {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-systemproperties-fields.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();
    }

    @Test
    public void testDefaults() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(System.getProperty("user.language"), gelfMessage.getField("propertyField1"));
        assertEquals("myproperty_IS_UNDEFINED", gelfMessage.getField("propertyField2"));
        assertEquals("otherproperty:fallback_IS_UNDEFINED", gelfMessage.getField("propertyField3"));
        assertEquals("embeddedmyproperty_IS_UNDEFINEDproperty", gelfMessage.getField("propertyField4"));
    }

    @Test
    public void testAfterSetProperties() throws Exception {

        System.setProperty(PROPERTY1, PROPERTY1_VALUE);
        System.setProperty(PROPERTY2, PROPERTY2_VALUE);

        setup();
        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(System.getProperty("user.language"), gelfMessage.getField("propertyField1"));
        assertEquals(PROPERTY1_VALUE, gelfMessage.getField("propertyField2"));
        assertEquals("otherproperty:fallback_IS_UNDEFINED", gelfMessage.getField("propertyField3"));
        assertEquals("embedded" + PROPERTY1_VALUE + "property", gelfMessage.getField("propertyField4"));
    }

}
