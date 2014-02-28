package biz.paluch.logging.gelf.logback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;

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
public class GelfLogbackAppenderDynamicMdcTest {

    LoggerContext lc = null;

    @Before
    public void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback-gelf-with-dynamic-fields.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.clear();
    }

    @Test
    public void testWithoutFields() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField("myMdc");
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put("myMdc", "value");
        MDC.put("myMdc-with-suffix1", "value1");
        MDC.put("myMdc-with-suffix2", "value2");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("value", gelfMessage.getField("myMdc"));
        assertEquals("value1", gelfMessage.getField("myMdc-with-suffix1"));
        assertEquals("value2", gelfMessage.getField("myMdc-with-suffix2"));

    }

    @Test
    public void testWithMdcRegex() throws Exception {

        Logger logger = lc.getLogger(getClass());
        MDC.put("someField", "included");
        MDC.put("someOtherField", "excluded");

        logger.info("Blubb Test");
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField("someField"));
        assertNull(gelfMessage.getField("someOtherField"));

    }

}
