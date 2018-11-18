package biz.paluch.logging.gelf.jboss7;

import static biz.paluch.logging.gelf.jboss7.JBoss7LogTestUtil.getJBoss7GelfLogHandler;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.logmanager.MDC;
import org.jboss.logmanager.NDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:36
 */
public class JBoss7GelfLogHandlerTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    @BeforeEach
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
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getField("NDC")).isEqualTo("ndc message");
        assertThat(gelfMessage.getField("MyTime")).isNotNull();
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name())).isEqualTo("testSimple");
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name())).isEqualTo(getClass().getName());

    }

    @Test
    public void testWarning() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.warning(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getLevel()).isEqualTo("4");
    }

    @Test
    public void testFine() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();
        handler.setLevel(Level.ALL);

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        logger.fine(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("7");
        GelfTestSender.getMessages().clear();

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("6");
        GelfTestSender.getMessages().clear();

        logger.warning(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("4");
        GelfTestSender.getMessages().clear();

        logger.severe(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("3");
        GelfTestSender.getMessages().clear();

    }

    @Test
    public void testSevere() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        NDC.clear();
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        NDC.push("ndc message");
        logger.severe(LOG_MESSAGE);
        NDC.clear();
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getLevel()).isEqualTo("3");

    }

    @Test
    public void testEmptyMessage() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info("");
        assertThat(GelfTestSender.getMessages()).isEmpty();

    }

    @Test
    public void testSimpleWithMsgFormatSubstitution() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message {0}";
        String expectedMessage = "foo bar test log message aaa";
        logger.log(Level.INFO, logMessage, new String[] { "aaa" });
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);

    }

    @Test
    public void testSimpleWithStringFormatSubstitution() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message %s";
        String expectedMessage = "foo bar test log message aaa";

        logger.log(Level.INFO, logMessage, new String[] { "aaa" });
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(expectedMessage);

    }

    @Test
    public void testFields() throws Exception {

        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put("mdcField1", "a value");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("fieldName1")).isEqualTo("fieldValue1");
        assertThat(gelfMessage.getField("fieldName2")).isEqualTo("fieldValue2");
        assertThat(gelfMessage.getField("mdcField1")).isEqualTo("a value");
        assertThat(gelfMessage.getField("mdcField2")).isNull();
    }

    @Test
    public void testWrongConfig() throws Exception {

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                JBoss7GelfLogHandler handler = new JBoss7GelfLogHandler();

                handler.setGraylogHost(null);
                handler.setGraylogPort(0);
            }
        });

    }
}
