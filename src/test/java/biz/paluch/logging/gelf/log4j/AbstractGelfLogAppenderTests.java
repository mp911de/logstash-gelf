package biz.paluch.logging.gelf.log4j;

import static biz.paluch.logging.gelf.GelfMessageAssembler.FIELD_STACK_TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:16
 */
public abstract class AbstractGelfLogAppenderTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        NDC.clear();
        NDC.push("ndc message");
        logger.info(LOG_MESSAGE);
        NDC.clear();
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getField("NDC")).isEqualTo("ndc message");
        assertThat(gelfMessage.getField("MyTime")).isNotNull();
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);
        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);

        // this is because of default-logstash-fields.properties
        assertThat(gelfMessage.getAdditonalFields().get("MySeverity")).isEqualTo("INFO");

    }

    @Test
    public void testLevels() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        String expectedMessage = "foo bar test log message";

        logger.trace(expectedMessage);
        assertThat(GelfTestSender.getMessages()).isEmpty();

        logger.debug(expectedMessage);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("7");
        GelfTestSender.getMessages().clear();

        logger.info(expectedMessage);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("6");
        GelfTestSender.getMessages().clear();

        logger.warn(expectedMessage);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("4");
        GelfTestSender.getMessages().clear();

        logger.error(expectedMessage);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("3");
        GelfTestSender.getMessages().clear();

        logger.fatal(expectedMessage);
        assertThat(GelfTestSender.getMessages().get(0).getLevel()).isEqualTo("2");
        GelfTestSender.getMessages().clear();

    }

    @Test
    public void testException() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE, new Exception("this is an exception"));
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(LOG_MESSAGE);
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.getFieldName()))
                .isEqualTo(AbstractGelfLogAppenderTests.class.getName());
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.getFieldName()))
                .isEqualTo("testException");

        assertThat(gelfMessage.getField(FIELD_STACK_TRACE)).contains("this is an exception");
        assertThat(gelfMessage.getField(FIELD_STACK_TRACE)).contains("skipped");
        assertThat(gelfMessage.getField(FIELD_STACK_TRACE)).contains("skipped");

    }

    @Test
    public void testFields() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        MDC.put("mdcField1", "a value");
        MDC.remove(GelfUtil.MDC_REQUEST_START_MS);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("fieldName1")).isEqualTo("fieldValue1");
        assertThat(gelfMessage.getField("fieldName2")).isEqualTo("fieldValue2");

        if (Log4jUtil.isLog4jMDCAvailable()) {
            assertThat(gelfMessage.getField("mdcField1")).isEqualTo("a value");
            assertThat(gelfMessage.getField("mdcField1")).isEqualTo("a value");
            assertThat(gelfMessage.getField("mdcField2")).isNull();

            assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION)).isNull();
            assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_END)).isNull();
        }
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceLineNumber.name())).isNotNull();
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name())).isEqualTo("testFields");
        assertThat(gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name()))
                .isEqualTo(AbstractGelfLogAppenderTests.class.getName());
    }

    @Test
    public void testProfiling() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "" + System.currentTimeMillis());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION)).isNotNull();
        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_END)).isNotNull();
    }

    @Test
    public void testLongProfiling() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "" + (System.currentTimeMillis() - 2000));

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION)).isNotNull();
        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_END)).isNotNull();
    }

    @Test
    public void testProfilingWrongStart() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "");

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION)).isNull();
        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_END)).isNull();

    }

    @Test
    public void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        logger.info(null, new IllegalStateException());

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException");
    }

    @Test
    public void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        logger.info("", new IllegalStateException("Help!"));

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
    }

    @Test
    public void testEmptyMessage() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        logger.info("");

        assertThat(GelfTestSender.getMessages()).isEmpty();
    }

}
