package biz.paluch.logging.gelf.jboss7;

import static biz.paluch.logging.gelf.jboss7.JBoss7LogTestUtil.*;
import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.intern.GelfMessage;
import org.jboss.logmanager.MDC;
import org.jboss.logmanager.NDC;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 08:36
 */
public class JBoss7GelfLogHandlerTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
        MDC.remove("mdcField1");
    }

    @Test
    public void testSimple() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        NDC.clear();
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        NDC.push("ndc message");
        logger.info(LOG_MESSAGE);
        NDC.clear();
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(GelfMessage.GELF_VERSION_1_1, gelfMessage.getVersion());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
        assertEquals("ndc message", gelfMessage.getField("NDC"));
        assertNotNull(gelfMessage.getField("MyTime"));
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());
        assertEquals("testSimple", gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name()));
        assertEquals(getClass().getName(), gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name()));

    }

    @Test
    public void testEmptyMessage() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info("");
        assertEquals(0, GelfTestSender.getMessages().size());

    }

    @Test
    public void testSimpleWithMsgFormatSubstitution() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message {0}";
        String expectedMessage = "foo bar test log message aaa";
        logger.log(Level.INFO, logMessage, new String[] { "aaa" });
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(expectedMessage, gelfMessage.getFullMessage());
        assertEquals(expectedMessage, gelfMessage.getShortMessage());
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());

    }

    @Test
    public void testSimpleWithStringFormatSubstitution() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message %s";
        String expectedMessage = "foo bar test log message aaa";

        logger.log(Level.INFO, logMessage, new String[] { "aaa" });
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(expectedMessage, gelfMessage.getFullMessage());
        assertEquals(expectedMessage, gelfMessage.getShortMessage());

    }

    @Test
    public void testFields() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put("mdcField1", "a value");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("fieldValue1", gelfMessage.getField("fieldName1"));
        assertEquals("fieldValue2", gelfMessage.getField("fieldName2"));
        assertEquals("a value", gelfMessage.getField("mdcField1"));
        assertNull(gelfMessage.getField("mdcField2"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongConfig() throws Exception {
        JBoss7GelfLogHandler handler = new JBoss7GelfLogHandler();

        handler.setGraylogHost(null);
        handler.setGraylogPort(0);

    }
}
