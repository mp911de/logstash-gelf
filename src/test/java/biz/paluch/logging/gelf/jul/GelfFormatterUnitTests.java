package biz.paluch.logging.gelf.jul;

import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.LogMessageField;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Greg Peterson
 * @author Mark Paluch
 */
public class GelfFormatterUnitTests {

    private Logger logger;

    @AfterAll
    public static void afterClass() {
        LogManager.getLogManager().reset();
    }

    @BeforeEach
    public void before() throws Exception {
        TestHandler.clear();
        LogManager.getLogManager().readConfiguration(GelfFormatterUnitTests.class.getResourceAsStream("/jul/test-gelf-formatter.properties"));
        logger = Logger.getLogger(GelfFormatterUnitTests.class.getName());
    }

    @Test
    public void test() {
        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        String[] loggedLines = TestHandler.getLoggedLines();
        assertThat(loggedLines.length).isEqualTo(3);
        assertThat(parseToJSONObject(loggedLines[0]).get("full_message")).isEqualTo("test1");
        assertThat(parseToJSONObject(loggedLines[1]).get("full_message")).isEqualTo("test2");
        assertThat(parseToJSONObject(loggedLines[2]).get("full_message")).isEqualTo("test3");
    }

    @Test
    public void testDefaults() {

        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        Map<String, Object> message = getMessage();

        assertThat(message.get("version")).isNull();
        assertThat(message).containsEntry("full_message", "test1");
        assertThat(message).containsEntry("short_message", "test1");
        assertThat(message).containsEntry("facility", "logstash-gelf");
        assertThat(message).containsEntry("level", "6");
        assertThat(message).containsEntry(LogMessageField.NamedLogField.SourceMethodName.name(), "testDefaults");
        assertThat(message).containsEntry(LogMessageField.NamedLogField.SourceClassName.name(), getClass().getName());
        assertThat(message).containsKeys("Thread", "timestamp", "MyTime");
    }

    @Test
    public void testConfigured() throws Exception {
        LogManager.getLogManager().readConfiguration(GelfFormatterUnitTests.class.getResourceAsStream("/jul/test-gelf-formatter-configured.properties"));

        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        Map<String, Object> message = getMessage();

        assertThat(message.get("version")).isNull();
        assertThat(message).containsEntry("full_message", "test1");
        assertThat(message).containsEntry("short_message", "test1");
        assertThat(message).containsEntry("facility", "test");
        assertThat(message).containsEntry("level", "6");
        assertThat(message).doesNotContainKeys("SourceLineNumber", "SourceMethodName", "SourceSimpleClassName",
                "SourceClassName");

        assertThat(message).containsEntry("fieldName1", "fieldValue1");
        assertThat(message).containsEntry("LoggerName", getClass().getName());

        assertThat(message).containsKeys("timestamp", "MyTime");
    }

    public Map<String, Object> getMessage() {
        String s = TestHandler.getLoggedLines()[0];
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
