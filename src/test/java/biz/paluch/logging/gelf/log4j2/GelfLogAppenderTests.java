package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;
    public static final String CONFIG_XML = "log4j2/log4j2.xml";

    private static LoggerContext loggerContext;

    protected static void reconfigure(String configXml) {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, configXml);
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        loggerContext.reconfigure();
    }

    @Before
    public void before() throws Exception {
        reconfigure(CONFIG_XML);
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    @Test
    public void testSimpleDebug() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        assertEquals(0, GelfTestSender.getMessages().size());
        logger.debug(LOG_MESSAGE);
        assertEquals(0, GelfTestSender.getMessages().size());

    }

    @Test
    public void testSimpleInfo() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(EXPECTED_LOG_MESSAGE, gelfMessage.getShortMessage());
        assertEquals(GelfMessage.GELF_VERSION_1_1, gelfMessage.getVersion());
        assertEquals("6", gelfMessage.getLevel());
        assertEquals(8192, gelfMessage.getMaximumMessageSize());

        assertEquals(RuntimeContainer.FQDN_HOSTNAME, gelfMessage.getField("server"));
        assertEquals(RuntimeContainer.HOSTNAME, gelfMessage.getField("server.simple"));
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, gelfMessage.getField("server.fqdn"));
        assertEquals(RuntimeContainer.ADDRESS, gelfMessage.getField("server.addr"));

        assertEquals(GelfLogAppenderTests.class.getSimpleName(), gelfMessage.getField("simpleClassName"));

    }

    @Test
    public void testFqdnHost() throws Exception {

        reconfigure("log4j2/log4j2-origin-host-fqdn.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, gelfMessage.getHost());
    }

    @Test
    public void testSimpleHost() throws Exception {

        reconfigure("log4j2/log4j2-origin-host-simple.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals(RuntimeContainer.HOSTNAME, gelfMessage.getHost());
    }

    @Test
    public void testCustomHost() throws Exception {

        reconfigure("log4j2/log4j2-origin-host-custom.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("my.custom.host", gelfMessage.getHost());
    }

    @Test
    public void testEmptyFacility() throws Exception {

        reconfigure("log4j2/log4j2-empty-facility.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("", gelfMessage.getFacility());
    }

    @Test
    public void testSimpleWarn() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.warn(LOG_MESSAGE);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("4", gelfMessage.getLevel());

    }

    @Test
    public void testSimpleError() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.error(LOG_MESSAGE);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("3", gelfMessage.getLevel());

    }

    @Test
    public void testSimpleFatal() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.fatal(LOG_MESSAGE);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("2", gelfMessage.getLevel());

    }

    @Test
    public void testMDC() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        ThreadContext.put("mdcField1", "my mdc value");
        ThreadContext.put(GelfUtil.MDC_REQUEST_START_MS, "" + System.currentTimeMillis());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("my mdc value", gelfMessage.getField("mdcField1"));

        assertNotNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION));
        assertNotNull(gelfMessage.getField(GelfUtil.MDC_REQUEST_END));

    }

    @Test
    public void testFactory() throws Exception {
        GelfLogAppender result = GelfLogAppender.createAppender(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, false);

        assertNull(result);

        result = GelfLogAppender.createAppender(null, "name", null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true);

        assertNull(result);

        result = GelfLogAppender.createAppender(null, "name", null, null, null, null, "host", null, null, null, null, null,
                null, null, null, null, null, null, false);

        assertNotNull(result);

        result = GelfLogAppender.createAppender(null, "name", null, null, null, null, "host", null, null, null, null, null,
                null, "facility", null, null, null, null, false);

        assertNotNull(result);
    }

    @Test
    public void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info((String) null, new IllegalStateException());

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("", new IllegalStateException("Help!"));

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getFullMessage());
        assertEquals("java.lang.IllegalStateException: Help!", gelfMessage.getShortMessage());
    }

    @Test
    public void testEmptyMessage() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("");

        assertEquals(0, GelfTestSender.getMessages().size());
    }
}
