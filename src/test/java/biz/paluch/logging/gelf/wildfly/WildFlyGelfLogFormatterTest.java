package biz.paluch.logging.gelf.wildfly;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.logmanager.MDC;
import org.jboss.logmanager.NDC;
import org.jboss.logmanager.handlers.WriterHandler;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.LogMessageField;

/**
 * @author Mark Paluch
 */
public class WildFlyGelfLogFormatterTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;
    private WriterHandler handler = new WriterHandler();
    private StringWriter stringWriter = new StringWriter();

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
        MDC.remove("mdcField1");
        NDC.clear();

        handler.setWriter(stringWriter);
    }

    @Test
    public void testDefaults() throws Exception {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        NDC.push("ndc message");
        logger.info(LOG_MESSAGE);
        NDC.clear();

        Map<String, Object> message = getMessage();

        assertNull(message.get("version"));
        assertEquals(EXPECTED_LOG_MESSAGE, message.get("full_message"));
        assertEquals(EXPECTED_LOG_MESSAGE, message.get("short_message"));
        assertEquals("ndc message", message.get("NDC"));
        assertEquals("logstash-gelf", message.get("facility"));
        assertEquals(getClass().getName(), message.get("LoggerName"));
        assertNotNull(message.get("Thread"));
        assertNotNull(message.get("timestamp"));
        assertNotNull(message.get("MyTime"));
        assertEquals("6", message.get("level"));
        assertEquals("testDefaults", message.get(LogMessageField.NamedLogField.SourceMethodName.name()));
        assertEquals(getClass().getName(), message.get(LogMessageField.NamedLogField.SourceClassName.name()));
    }

    @Test
    public void testEmptyMessage() throws Exception {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info("");
        Map<String, Object> message = getMessage();

        assertNull(message.get("full_message"));
        assertNull(message.get("short_message"));

    }

    @Test
    public void testSimpleWithMsgFormatSubstitution() throws Exception {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message {0}";
        String expectedMessage = "foo bar test log message aaa";
        logger.log(Level.INFO, logMessage, new String[] { "aaa" });

        Map<String, Object> message = getMessage();

        assertEquals(expectedMessage, message.get("full_message"));
        assertEquals(expectedMessage, message.get("short_message"));
    }

    @Test
    public void testSimpleWithStringFormatSubstitution() throws Exception {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message %s";
        String expectedMessage = "foo bar test log message aaa";

        logger.log(Level.INFO, logMessage, new String[] { "aaa" });

        Map<String, Object> message = getMessage();

        assertEquals(expectedMessage, message.get("full_message"));
        assertEquals(expectedMessage, message.get("short_message"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownField() throws Exception {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setFields("dummy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotSupportedField() throws Exception {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setFields("Marker");
    }

    @Test
    public void testFields() throws Exception {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setFields("Time,Severity,ThreadName,SourceSimpleClassName,NDC");

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);

        Map<String, Object> message = getMessage();

        assertNotNull(message.get("SourceSimpleClassName"));
        assertNull(message.get("LoggerName"));
    }

    @Test
    public void testLineBreak() throws Exception {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setLineBreak("XxX");

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);
        logger.info(LOG_MESSAGE);
        assertTrue(stringWriter.getBuffer().toString().contains("}XxX{"));
    }

    @Test
    public void testMdcFields() throws Exception {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setOriginHost("myhost");
        formatter.setAdditionalFields("fieldName1=fieldValue1,fieldName2=fieldValue2");
        formatter.setDynamicMdcFields(".*");
        formatter.setIncludeFullMdc(true);

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put("mdcField1", "a value");

        logger.info(LOG_MESSAGE);

        Map<String, Object> message = getMessage();

        assertEquals("myhost", message.get("host"));
        assertEquals("fieldValue1", message.get("fieldName1"));
        assertEquals("fieldValue2", message.get("fieldName2"));
        assertEquals("a value", message.get("mdcField1"));
    }

    @Test
    public void testException() throws Exception {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setOriginHost("myhost");
        formatter.setExtractStackTrace(true);

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.log(Level.WARNING, LOG_MESSAGE, new Exception("boom!"));

        Map<String, Object> message = getMessage();

        assertThat(message.get("StackTrace").toString(), containsString("boom!"));
    }

    public Map<String, Object> getMessage() {
        return JsonUtil.parseToMap(stringWriter.getBuffer().toString());
    }
}
