package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import biz.paluch.logging.gelf.LogMessageField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class GelfLogAppenderMinimalTest {
    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

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
        ThreadContext.clearAll();
    }

    @Test
    public void testSimpleDebug() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        assertEquals(0, GelfTestSender.getMessages().size());
        logger.debug(LOG_MESSAGE);
        assertEquals(0, GelfTestSender.getMessages().size());

    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(new MarkerManager.Log4jMarker("test"), LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
        assertNotNull(gelfMessage.getField("MyTime"));
        assertEquals("test", gelfMessage.getAdditonalFields().get("Marker"));
        assertEquals("6", gelfMessage.getLevel());

        assertNotNull(gelfMessage.getField(LogMessageField.NamedLogField.SourceLineNumber.name()));
        assertEquals("testSimpleInfo", gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name()));
        assertEquals(getClass().getName(), gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name()));

    }
}
