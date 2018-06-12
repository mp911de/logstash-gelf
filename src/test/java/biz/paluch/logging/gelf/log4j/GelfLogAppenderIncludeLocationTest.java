package biz.paluch.logging.gelf.log4j;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderIncludeLocationTest {

    public static final String LOG_MESSAGE = "foo bar test log message";

    @BeforeEach
    public void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        DOMConfigurator.configure(getClass().getResource("/log4j/log4j-without-location.xml"));
    }

    @Test
    public void testWithoutLocation() throws Exception {

        Logger logger = Logger.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("SourceClassName")).isNull();
        assertThat(gelfMessage.getField("SourceSimpleClassName")).isNull();
        assertThat(gelfMessage.getField("SourceMethodName")).isNull();
        assertThat(gelfMessage.getField("SourceLineNumber")).isNull();
    }
}
