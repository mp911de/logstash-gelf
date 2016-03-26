package biz.paluch.logging.gelf.jul;

import static org.junit.Assert.assertEquals;
import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerSystemPropertiesTest {
    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String PROPERTY1 = "myproperty";
    public static final String PROPERTY1_VALUE = "value of myproperty";

    public static final String PROPERTY2 = "otherproperty";
    public static final String PROPERTY2_VALUE = "value of otherproperty";

    @Before
    public void before() throws Exception {

        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().readConfiguration(
                getClass().getResourceAsStream("/jul/test-logging-systemproperties.properties"));
    }

    @Test
    public void testDefaults() throws Exception {

        Logger logger = Logger.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("${user.language}", gelfMessage.getField("propertyField1"));
        assertEquals("${myproperty}", gelfMessage.getField("propertyField2"));
        assertEquals("${otherproperty:fallback}", gelfMessage.getField("propertyField3"));
        assertEquals("embedded${myproperty}property", gelfMessage.getField("propertyField4"));
    }

}
