package biz.paluch.logging.gelf.logback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * @author Mark Paluch
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 27.09.13 08:16
 */
abstract class AbstractGelfLogAppenderTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    LoggerContext lc = null;

    @Test
    void testLevels() throws Exception {

        Logger logger = lc.getLogger(getClass());

        assertThat(GelfTestSender.getMessages()).isEmpty();
        logger.debug(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).isEmpty();

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("6");
        GelfTestSender.getMessages().clear();

        logger.warn(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("4");
        GelfTestSender.getMessages().clear();

        logger.error(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("3");
        GelfTestSender.getMessages().clear();

        logger.log(null, getClass().getName(), 0, LOG_MESSAGE, new Object[0], null);
        assertThat(GelfTestSender.getMessages()).isEmpty();

        logger.log(null, getClass().getName(), 10, LOG_MESSAGE, new Object[0], null);
        assertThat(GelfTestSender.getMessages()).isEmpty();

        logger.log(null, getClass().getName(), 20, LOG_MESSAGE, new Object[0], null);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("6");
        GelfTestSender.getMessages().clear();

        logger.log(null, getClass().getName(), 30, LOG_MESSAGE, new Object[0], null);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("4");
        GelfTestSender.getMessages().clear();

        logger.log(null, getClass().getName(), 40, LOG_MESSAGE, new Object[0], null);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("3");
        GelfTestSender.getMessages().clear();
    }

    @Test
    void testSimpleInfo() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        assertThat(gelfMessage.getField("MyTime")).isNotNull();
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);

    }

    @Test
    void testMarker() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(MarkerFactory.getMarker("basic"), LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getAdditonalFields().get("Marker")).isEqualTo("basic");
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);

    }

    @Test
    void testException() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE, new Exception("this is an exception"));
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.getFieldName()))
                .isEqualTo(AbstractGelfLogAppenderTests.class.getName());
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.getFieldName()))
                .isEqualTo("testException");

        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE)).contains("this is an exception");

    }

    @Test
    void testFields() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put("mdcField1", "a value");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("fieldName1")).isEqualTo("fieldValue1");
        assertThat(gelfMessage.getField("fieldName2")).isEqualTo("fieldValue2");
        assertThat(gelfMessage.getField("mdcField1")).isEqualTo("a value");
        assertThat(gelfMessage.getField("mdcField2")).isNull();

        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceLineNumber.name())).isNotNull();
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name())).isEqualTo("testFields");
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name()))
                .isEqualTo(AbstractGelfLogAppenderTests.class.getName());

    }

    @Test
    void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = lc.getLogger(getClass());

        logger.info(null, new IllegalStateException());

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException");
    }

    @Test
    void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = lc.getLogger(getClass());

        logger.info("", new IllegalStateException("Help!"));

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
    }

    @Test
    void testEmptyMessage() throws Exception {
        Logger logger = lc.getLogger(getClass());

        logger.info("");

        assertThat(GelfTestSender.getMessages()).isEmpty();
    }

}
