package biz.paluch.logging.gelf.log4j;

import org.apache.log4j.LogManager;
import org.apache.log4j.MDC;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;

import biz.paluch.logging.gelf.GelfTestSender;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 07:47
 */
public class GelfLogAppenderXmlTest extends AbstractGelfLogAppenderTest {

    @Before
    public void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        DOMConfigurator.configure(getClass().getResource("/log4j.xml"));
        MDC.remove("mdcField1");
    }

}
