package biz.paluch.logging.gelf.log4j;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.LogMessageField;

/**
 * @author <a href="mailto:kai.geisselhardt@kaufland.com">Kai Geisselhardt</a>
 */
public class GelfLayoutTest {

    private Logger logger;

    @BeforeAll
    public static void beforeClass() {
        DOMConfigurator.configure(GelfLayoutTest.class.getResource("/log4j/log4j-gelf-layout.xml"));
    }

    @BeforeEach
    public void before() {
        TestAppender.clearLoggedLines();
        logger = Logger.getLogger(GelfLayoutTest.class);
    }

    @Test
    public void test() {

        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        String[] loggedLines = TestAppender.getLoggedLines();
        assertThat(loggedLines.length).isEqualTo(3);
        assertThat(parseToJSONObject(loggedLines[0]).get("full_message")).isEqualTo("test1");
        assertThat(parseToJSONObject(loggedLines[1]).get("full_message")).isEqualTo("test2");
        assertThat(parseToJSONObject(loggedLines[2]).get("full_message")).isEqualTo("test3");
    }

    @Test
    public void testDefaults() throws Exception {

        NDC.push("ndc message");
        logger.info("test1");
        logger.info("test2");
        logger.info("test3");
        NDC.clear();

        Map<String, Object> message = getMessage();

        assertNull(message.get("version"));
        assertEquals("test1", message.get("full_message"));
        assertEquals("test1", message.get("short_message"));
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
    public void testConfiguration() throws Exception {

        logger = Logger.getLogger("biz.paluch.logging.gelf.log4j.configured");

        MDC.put("mdcField1", "mdcValue1");
        NDC.push("ndc message");
        logger.info("test1");
        logger.info("test2");
        logger.info("test3");
        NDC.clear();

        Map<String, Object> message = getMessage();

        assertNull(message.get("version"));
        assertEquals("test1", message.get("full_message"));
        assertEquals("test1", message.get("short_message"));
        assertEquals("ndc message", message.get("NDC"));
        assertEquals("fieldValue1", message.get("fieldName1"));

        if (Log4jUtil.isLog4jMDCAvailable()) {
            assertEquals("mdcValue1", message.get("mdcField1"));
        }

        assertEquals("test", message.get("facility"));
        assertEquals("biz.paluch.logging.gelf.log4j.configured", message.get("LoggerName"));
        assertNull(message.get("Thread"));
        assertNotNull(message.get("timestamp"));
        assertNotNull(message.get("MyTime"));
        assertEquals("6", message.get("level"));
        assertNull(message.get(LogMessageField.NamedLogField.SourceMethodName.name()));
        assertNull(message.get(LogMessageField.NamedLogField.SourceClassName.name()));
    }

    public Map<String, Object> getMessage() {
        String s = TestAppender.getLoggedLines()[0];
        try {
            return (Map) JsonUtil.parseToMap(s);
        } catch (RuntimeException e) {
            System.out.println("Trying to parse: " + s);
            throw e;
        }
    }

    private Map<String, Object> parseToJSONObject(String value) {
        return JsonUtil.parseToMap(value);
    }
}