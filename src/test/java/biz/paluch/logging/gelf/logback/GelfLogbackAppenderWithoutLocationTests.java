package biz.paluch.logging.gelf.logback;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author Mark Paluch
 */
class GelfLogbackAppenderWithoutLocationTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private LoggerContext lc = null;

    @BeforeEach
    void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-without-location.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.clear();
    }

    @Test
    void testWithoutLocation() {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("SourceClassName")).isNull();
        assertThat(gelfMessage.getField("SourceSimpleClassName")).isNull();
        assertThat(gelfMessage.getField("SourceMethodName")).isNull();
        assertThat(gelfMessage.getField("SourceLineNumber")).isNull();
    }
}
