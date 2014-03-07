package biz.paluch.logging.gelf.logback;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.slf4j.MDC;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 27.09.13 08:16
 */
public abstract class AbstractGelfLogAppenderTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    LoggerContext lc = null;

    @Test
    public void testSimpleDebug() throws Exception {

        Logger logger = lc.getLogger(getClass());

        assertEquals(0, GelfTestSender.getMessages().size());
        logger.debug(LOG_MESSAGE);
        assertEquals(0, GelfTestSender.getMessages().size());

    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
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
        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE), containsString("skipped"));
        assertThat(gelfMessage.getField(MdcGelfMessageAssembler.FIELD_STACK_TRACE), containsString("skipped"));

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

    }
}
