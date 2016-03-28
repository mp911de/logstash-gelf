package biz.paluch.logging.gelf.logback;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

/**
 * @author Mark Paluch
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 27.09.13 08:16
 */
public abstract class AbstractGelfLogAppenderTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    LoggerContext lc = null;

    @Test
    public void testLevels() throws Exception {

        Logger logger = lc.getLogger(getClass());

        assertEquals(0, GelfTestSender.getMessages().size());
        logger.debug(LOG_MESSAGE);
        assertEquals(0, GelfTestSender.getMessages().size());

        logger.info(LOG_MESSAGE);
        assertEquals("6", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.warn(LOG_MESSAGE);
        assertEquals("4", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.error(LOG_MESSAGE);
        assertEquals("3", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.log(null, getClass().getName(), 0, LOG_MESSAGE, new Object[0], null);
        assertEquals(0, GelfTestSender.getMessages().size());

        logger.log(null, getClass().getName(), 10, LOG_MESSAGE, new Object[0], null);
        assertEquals(0, GelfTestSender.getMessages().size());

        logger.log(null, getClass().getName(), 20, LOG_MESSAGE, new Object[0], null);
        assertEquals("6", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.log(null, getClass().getName(), 30, LOG_MESSAGE, new Object[0], null);
        assertEquals("4", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();

        logger.log(null, getClass().getName(), 40, LOG_MESSAGE, new Object[0], null);
        assertEquals("3", GelfTestSender.getMessages().get(0).getLevel());
        GelfTestSender.getMessages().clear();
    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
        assertEquals(GelfMessage.GELF_VERSION_1_1, gelfMessage.getVersion());
        assertNotNull(gelfMessage.getField("MyTime"));
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());

    }

    @Test
    public void testMarker() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(MarkerFactory.getMarker("basic"), LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
        assertEquals("basic", gelfMessage.getAdditonalFields().get("Marker"));
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());

    }

    @Test
    public void testException() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE, new Exception("this is an exception"));
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(AbstractGelfLogAppenderTest.class.getName(),
                gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.getFieldName()));
        assertEquals("testException", gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.getFieldName()));

        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE), containsString("this is an exception"));

    }

    @Test
    public void testFields() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put("mdcField1", "a value");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("fieldValue1", gelfMessage.getField("fieldName1"));
        assertEquals("fieldValue2", gelfMessage.getField("fieldName2"));
        assertEquals("a value", gelfMessage.getField("mdcField1"));
        assertNull(gelfMessage.getField("mdcField2"));

        assertNotNull(gelfMessage.getField(LogMessageField.NamedLogField.SourceLineNumber.name()));
        assertEquals("testFields", gelfMessage.getField(LogMessageField.NamedLogField.SourceMethodName.name()));
        assertEquals(AbstractGelfLogAppenderTest.class.getName(),
                gelfMessage.getField(LogMessageField.NamedLogField.SourceClassName.name()));

    }

    @Test
    public void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = lc.getLogger(getClass());

        logger.info(null, new IllegalStateException());

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = lc.getLogger(getClass());

        logger.info("", new IllegalStateException("Help!"));

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessage() throws Exception {
        Logger logger = lc.getLogger(getClass());

        logger.info("");

        assertEquals(0, GelfTestSender.getMessages().size());
    }

}
