package biz.paluch.logging.gelf.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.MDC;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
public class GelfLogHandlerTests {
    @Before
    public void before() throws Exception {

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-logging.properties"));
        MDC.remove("mdcField1");
    }

    private void assertExpectedMessage(String expectedMessage) {
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(GelfMessage.GELF_VERSION_1_1, gelfMessage.getVersion());
        assertNotNull(gelfMessage.getField("MyTime"));
        assertEquals(expectedMessage, gelfMessage.getFullMessage());
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(expectedMessage, gelfMessage.getShortMessage());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());
    }

    @Test
    public void testWithoutResourceBundle() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, expectedMessage, params);

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("a", gelfMessage.getField("MessageParam0"));
        assertEquals("b", gelfMessage.getField("MessageParam1"));

        assertExpectedMessage(expectedMessage);

    }

    @Test
    public void testWithoutMessageParameters() throws Exception {

        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-logging-without-message-parameters.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, expectedMessage, params);

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertNull(gelfMessage.getField("MessageParam0"));
        assertNull(gelfMessage.getField("MessageParam1"));
    }

    @Test
    public void testWithResourceBundleFormattingWithCurlyBrackets() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "params a and b";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, "message.format.curly.brackets", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    public void testWithResourceBundleFormattingWithoutParameters() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "no parameter supplied";

        logger.log(Level.INFO, "message.format.withoutParameter");

        assertExpectedMessage(expectedMessage);
    }

    @Test
    public void testWithResourceBundleFormattingMalformed1() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "message.format.fail1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, "message.format.fail1", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    public void testWithResourceBundleFormattingMalformed2() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "message.format.fail2";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, "message.format.fail2", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    public void testWithResourceBundleFormattingWithPercentages() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "params a and 1";

        Object[] params = new Object[] { "a", 1, "c" };
        logger.log(Level.INFO, "message.format.percentages", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    public void testSimpleInfo() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.info(expectedMessage);
        assertExpectedMessage(expectedMessage);

    }

    @Test
    public void testSimpleNull() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = null;
        logger.info(expectedMessage);

        assertEquals(0, GelfTestSender.getMessages().size());
    }

    @Test
    public void testSimpleWarning() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.warning(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("4", gelfMessage.getLevel());

    }

    @Test
    public void testSimpleSevere() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.severe(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("3", gelfMessage.getLevel());

    }

    @Test
    public void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, null, new IllegalStateException());

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, "", new IllegalStateException("Help!"));

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessage() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.info("");

        assertEquals(0, GelfTestSender.getMessages().size());
    }

}
