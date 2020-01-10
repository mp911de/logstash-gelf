package biz.paluch.logging.gelf.jboss7;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static biz.paluch.logging.gelf.jboss7.JBoss7LogTestUtil.getJBoss7GelfLogHandler;
import static org.assertj.core.api.Assertions.assertThat;

public class Jboss7GelfLogHandlerMessageFormatTests {

    @BeforeEach
    public void beforeEach() {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
    }

    @ParameterizedTest
    @CsvSource({
                       "foo bar %s,     foo bar aaa",
                       "foo bar '%s',   foo bar 'aaa'",
                       "foo bar ''%s'', foo bar ''aaa''",
                       "foo bar {0},    foo bar aaa",
                       "%sdfsdfk#! {0}, %sdfsdfk#! aaa"
               })
    public void testMessageFormatting(String logMessage, String expectedMessage) {
        JBoss7GelfLogHandler handler = getJBoss7GelfLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.log(Level.INFO, logMessage, new String[] { "aaa" });
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(expectedMessage);
    }
}
