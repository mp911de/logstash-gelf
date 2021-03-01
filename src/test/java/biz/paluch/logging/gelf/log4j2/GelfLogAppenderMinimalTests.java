package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderMinimalTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    private static LoggerContext loggerContext;

    @BeforeAll
    static void beforeClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-minimal.xml");
        PropertiesUtil.getProperties().reload();
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        PropertiesUtil.getProperties().reload();
        loggerContext.reconfigure();
    }

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    @Test
    void testSimpleDebug() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        assertThat(GelfTestSender.getMessages()).isEmpty();
        logger.debug(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).isEmpty();

    }

    @Test
    void testSimpleInfo() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(new MarkerManager.Log4jMarker("test"), LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getField("MyTime")).isNotNull();
        assertThat(gelfMessage.getAdditonalFields().get("Marker")).isEqualTo("test");
        assertThat(gelfMessage.getLevel()).isEqualTo("6");

        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceLineNumber.name())).isNotNull();
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name())).isEqualTo("testSimpleInfo");
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name())).isEqualTo(getClass().getName());

    }
}
