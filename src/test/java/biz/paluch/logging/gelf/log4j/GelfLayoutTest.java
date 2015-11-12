package biz.paluch.logging.gelf.log4j;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.logging.gelf.LogMessageField;

public class GelfLayoutTest {

    private static Logger logger;

    @BeforeClass
    public static void beforeClass() {
        DOMConfigurator.configure(GelfLayoutTest.class.getResource("/log4j-gelf-layout.xml"));
        logger = Logger.getLogger(GelfLayoutTest.class);
    }

    @Before
    public void before() {
        TestAppender.clearLoggedLines();
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

    public Map<String, Object> getMessage() {
        try {
            return (Map) new JSONParser().parse(TestAppender.getLoggedLines()[0]);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private JSONObject parseToJSONObject(String value) {
        return (JSONObject) JSONValue.parse(value);
    }
}