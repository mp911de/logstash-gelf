package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 */
public class GelfLogAppenderDynamicMdcTest {
    private static LoggerContext loggerContext;

    @BeforeClass
    public static void setupClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2-dynamic-mdc.xml");
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        loggerContext.reconfigure();
    }

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clear();
    }

    @Test
    public void testWithoutFields() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField("myMdc");
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put("myMdc", "value");
        ThreadContext.put("myMdc-with-suffix1", "value1");
        ThreadContext.put("myMdc-with-suffix2", "value2");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("value", gelfMessage.getField("myMdc"));
        assertEquals("value1", gelfMessage.getField("myMdc-with-suffix1"));
        assertEquals("value2", gelfMessage.getField("myMdc-with-suffix2"));

    }

    @Test
    public void testWithMdcRegex() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put("someField", "included");
        ThreadContext.put("someOtherField", "excluded");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField("someField"));
        assertNull(gelfMessage.getField("someOtherField"));
    }
}
