package biz.paluch.logging.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        assertThat(gelfMessage.getField("MyTime")).isNotNull();
        assertThat(gelfMessage.getFullMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getShortMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);
    }

    @Test
    public void testWithoutResourceBundle() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, expectedMessage, params);

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("MessageParam0")).isEqualTo("a");
        assertThat(gelfMessage.getField("MessageParam1")).isEqualTo("b");

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

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("MessageParam0")).isNull();
        assertThat(gelfMessage.getField("MessageParam1")).isNull();
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

        assertThat(GelfTestSender.getMessages()).isEmpty();
    }

    @Test
    public void testSimpleWarning() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.warning(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getLevel()).isEqualTo("4");

    }

    @Test
    public void testSimpleSevere() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.severe(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getLevel()).isEqualTo("3");

    }

    @Test
    public void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, null, new IllegalStateException());

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException");
    }

    @Test
    public void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, "", new IllegalStateException("Help!"));

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
    }

    @Test
    public void testEmptyMessage() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.info("");

        assertThat(GelfTestSender.getMessages()).isEmpty();
    }

}
