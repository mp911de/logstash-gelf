package biz.paluch.logging.gelf.log4j;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Level;
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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 08:16
 */
public abstract class AbstractGelfLogAppenderTest {

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

        // this is because of default-logstash-fields.properties
        assertEquals("INFO", gelfMessage.getAdditonalFields().get("MySeverity"));

    }

    @Test
    public void testSimpleWarn() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        String expectedMessage = "foo bar test log message";
        logger.warn(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("4", gelfMessage.getLevel());

    }

    @Test
    public void testSimpleError() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        String expectedMessage = "foo bar test log message";
        logger.error(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("3", gelfMessage.getLevel());
    }

    @Test
    public void testSimpleFatal() throws Exception {
        Logger logger = Logger.getLogger(getClass());

        String expectedMessage = "foo bar test log message";
        logger.fatal(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("2", gelfMessage.getLevel());
    }

    @Test
    public void testSimpleDebug() throws Exception {
        Logger logger = Logger.getLogger(getClass());
        logger.setLevel(Level.ALL);

        String expectedMessage = "foo bar test log message";
        logger.debug(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("7", gelfMessage.getLevel());
    }

    @Test
    public void testSimpleTrace() throws Exception {
        Logger logger = Logger.getLogger(getClass());
        logger.setLevel(Level.ALL);

        String expectedMessage = "foo bar test log message";
        logger.trace(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("7", gelfMessage.getLevel());
    }

    @Test
    public void testException() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE, new Exception("this is an exception"));
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(AbstractGelfLogAppenderTest.class.getName(),
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
        assertEquals("a value", gelfMessage.getField("mdcField1"));
        assertNull(gelfMessage.getField("mdcField2"));

        assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
        assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));

    }

    @Test
    public void testProfiling() throws Exception {

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

        Logger logger = Logger.getLogger(getClass());
        MDC.put(GelfUtil.MDC_REQUEST_START_MS, "");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
        assertNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));

    }
}
