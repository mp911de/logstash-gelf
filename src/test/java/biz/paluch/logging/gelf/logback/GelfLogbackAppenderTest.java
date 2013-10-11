package biz.paluch.logging.gelf.logback;

import java.net.URL;

import org.junit.Before;
import org.slf4j.MDC;

import biz.paluch.logging.gelf.log4j.GelfTestSender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-07
 */
public class GelfLogbackAppenderTest extends AbstractGelfLogAppenderTest {

    @Before
    public void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.remove("mdcField1");
    }
}
