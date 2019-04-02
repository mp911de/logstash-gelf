package biz.paluch.logging.gelf.log4j;

import static org.assertj.core.api.Assertions.assertThat;

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
 * @author Mark Paluch
 */
class GelfLayoutUnitTests {

    private Logger logger;

    @BeforeAll
    static void beforeClass() {
        DOMConfigurator.configure(GelfLayoutUnitTests.class.getResource("/log4j/log4j-gelf-layout.xml"));
    }

    @BeforeEach
    void before() {
        TestAppender.clearLoggedLines();
        logger = Logger.getLogger(GelfLayoutUnitTests.class);
    }

    @Test
    void test() {

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
    void testDefaults() {

        NDC.push("ndc message");
        logger.info("test1");
        logger.info("test2");
        logger.info("test3");
        NDC.clear();

        Map<String, Object> message = getMessage();

        assertThat(message.get("version")).isNull();
        assertThat(message).containsEntry("full_message", "test1");
        assertThat(message).containsEntry("short_message", "test1");
        assertThat(message).containsEntry("NDC", "ndc message");
        assertThat(message).containsEntry("facility", "logstash-gelf");
        assertThat(message).containsEntry("level", "6");
        assertThat(message).containsEntry(LogMessageField.NamedLogField.SourceMethodName.name(), "testDefaults");
        assertThat(message).containsEntry(LogMessageField.NamedLogField.SourceClassName.name(), getClass().getName());
        assertThat(message).containsKeys("Thread", "timestamp", "MyTime");
    }

    @Test
    void testConfiguration() {

        logger = Logger.getLogger("biz.paluch.logging.gelf.log4j.configured");

        MDC.put("mdcField1", "mdcValue1");
        NDC.push("ndc message");
        logger.info("test1");
        logger.info("test2");
        logger.info("test3");
        NDC.clear();

        Map<String, Object> message = getMessage();

        assertThat(message.get("version")).isNull();
        assertThat(message).containsEntry("full_message", "test1");
        assertThat(message).containsEntry("short_message", "test1");
        assertThat(message).containsEntry("NDC", "ndc message");
        assertThat(message).containsEntry("facility", "test");
        assertThat(message).containsEntry("level", "6");
        assertThat(message).doesNotContainKeys("SourceLineNumber", "SourceMethodName", "SourceSimpleClassName",
                "SourceClassName");

        assertThat(message).containsEntry("fieldName1", "fieldValue1");
        assertThat(message).containsEntry("LoggerName", "biz.paluch.logging.gelf.log4j.configured");
        if (Log4jUtil.isLog4jMDCAvailable()) {
            assertThat(message).containsEntry("mdcField1", "mdcValue1");
        }

        assertThat(message).containsKeys("timestamp", "MyTime");
    }

    Map<String, Object> getMessage() {
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
