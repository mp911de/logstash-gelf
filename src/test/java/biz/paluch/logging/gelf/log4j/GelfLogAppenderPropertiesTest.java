package biz.paluch.logging.gelf.log4j;

import org.apache.log4j.LogManager;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;

import biz.paluch.logging.gelf.GelfTestSender;

/**
 * @author Mark Paluch
 * @since 27.09.13 07:47
 */
public class GelfLogAppenderPropertiesTest extends AbstractGelfLogAppenderTest {

    @Before
    public void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        PropertyConfigurator.configure(getClass().getResource("/log4j/log4j-test.properties"));
        MDC.remove("mdcField1");
    }

}
