package biz.paluch.logging.gelf.log4j;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
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
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
        assertEquals("ndc message", gelfMessage.getField("NDC"));
        assertNotNull(gelfMessage.getField("MyTime"));
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());
        assertEquals(GelfMessage.GELF_VERSION_1_1, gelfMessage.getVersion());

        // this is because of default-logstash-fields.properties
        assertEquals("INFO", gelfMessage.getAdditonalFields().get("MySeverity"));

    }

    @Test
    public void testLevels() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        String expectedMessage = "foo bar test log message";

        logger.trace(expectedMessage);
        assertEquals(0, GelfTestSender.getMessages().size());

        logger.debug(expectedMessage);
        assertEquals("7", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.info(expectedMessage);
        assertEquals("6", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.warn(expectedMessage);
        assertEquals("4", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.error(expectedMessage);
        assertEquals("3", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.fatal(expectedMessage);
        assertEquals("2", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

    }

    @Test
    public void testException() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE, new Exception("this is an exception"));
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(AbstractGelfLogAppenderTests.class.getName(),
                gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.getFieldName()));
        assertEquals("testException", gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.getFieldName()));

        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE), containsString("this is an exception"));
        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE), containsString("skipped"));
        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE), containsString("skipped"));

    }

    @Test
    public void testFields() throws Exception {

        Logger logger = Logger.getLogger(getClass());
        MDC.put("mdcField1", "a value");
        MDC.remove(GelfUtil.MDC_REQUEST_START_MS);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("fieldValue1", gelfMessage.getField("fieldName1"));
        assertEquals("fieldValue2", gelfMessage.getField("fieldName2"));

        if(Log4jUtil.isLog4jMDCAvailable()) {
            assertEquals("a value", gelfMessage.getField("mdcField1"));
            assertEquals("a value", gelfMessage.getField("mdcField1"));
            assertNull(gelfMessage.getField("mdcField2"));

            assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
            assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));
        }
        assertNotNull(gelfMessage.getField(LogMessageField.NamedLogField.SourceLineNumber.name()));
        assertEquals("testFields", gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name()));
        assertEquals(AbstractGelfLogAppenderTests.class.getName(),
                gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name()));
    }

    @Test
    public void testProfiling() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "" + System.currentTimeMillis());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertNotNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
        assertNotNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));
    }

    @Test
    public void testLongProfiling() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "" + (System.currentTimeMillis() - 2000));

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertNotNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
        assertNotNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));
    }

    @Test
    public void testProfilingWrongStart() throws Exception {

        assumeTrue(Log4jUtil.isLog4jMDCAvailable());

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
        assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));

    }

    @Test
    public void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        logger.info(null, new IllegalStateException());

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        logger.info("", new IllegalStateException("Help!"));

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessage() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        logger.info("");

        assertEquals(0, GelfTestSender.getMessages().size());
    }

}
