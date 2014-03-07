package biz.paluch.logging.gelf.logback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-07
 */
public class GelfLogbackAppenderDynamicHostnameTest {

    public static final String LOG_MESSAGE = "foo bar test log message";
    LoggerContext lc = null;

    @Before
    public void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback-gelf-with-dynamic-originhost.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.remove("mdcField1");
    }

    @Test
    public void testOriginHost() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String crossCheckHostName = gelfMessage.getAdditonalFields().get("crossCheckHostName");

        String json = gelfMessage.toJson();
        assertThat(json, StringContains.containsString("\"_crossCheckHostName\":\"" + crossCheckHostName + "\""));
        assertThat(json, StringContains.containsString("\"host\":\"" + crossCheckHostName + "\""));
    }

}
