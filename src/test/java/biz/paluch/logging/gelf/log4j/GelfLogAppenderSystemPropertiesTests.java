package biz.paluch.logging.gelf.log4j;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 16.07.14 17:30
 */
public class GelfLogAppenderSystemPropertiesTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String PROPERTY1 = "myproperty";
    public static final String PROPERTY1_VALUE = "value of myproperty";

    public static final String PROPERTY2 = "otherproperty";
    public static final String PROPERTY2_VALUE = "value of otherproperty";

    @Before
    public void before() throws Exception {
        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        setup();

    }

    protected void setup() {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        DOMConfigurator.configure(getClass().getResource("/log4j/log4j-with-system-properties.xml"));
    }

    @Test
    public void testDefaults() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(System.getProperty("user.language"), gelfMessage.getField("propertyField1"));
        assertEquals("", gelfMessage.getField("propertyField2"));
        assertEquals("", gelfMessage.getField("propertyField3"));
        assertEquals("embeddedproperty", gelfMessage.getField("propertyField4"));
    }

    @Test
    public void testAfterSetProperties() throws Exception {

        System.setProperty(PROPERTY1, PROPERTY1_VALUE);
        System.setProperty(PROPERTY2, PROPERTY2_VALUE);

        setup();
        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(System.getProperty("user.language"), gelfMessage.getField("propertyField1"));
        assertEquals(PROPERTY1_VALUE, gelfMessage.getField("propertyField2"));
        assertEquals("", gelfMessage.getField("propertyField3"));
        assertEquals("embedded" + PROPERTY1_VALUE + "property", gelfMessage.getField("propertyField4"));
    }

}
