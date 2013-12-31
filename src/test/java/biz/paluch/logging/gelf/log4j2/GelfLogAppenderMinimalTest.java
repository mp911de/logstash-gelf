package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;

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
public class GelfLogAppenderMinimalTest {
    private static LoggerContext loggerContext;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2-minimal.xml");
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
    public void testSimpleDebug() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        assertEquals(0, GelfTestSender.getMessages().size());
        logger.debug("Blubb Test");
        assertEquals(0, GelfTestSender.getMessages().size());

    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("Blubb Test", gelfMessage.getFullMessage());
        assertEquals("6", gelfMessage.getLevel());
        assertEquals("Blubb Test", gelfMessage.getShortMessage());

    }
}
