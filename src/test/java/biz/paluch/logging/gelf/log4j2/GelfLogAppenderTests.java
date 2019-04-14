package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;
    private static final String CONFIG_XML = "log4j2/log4j2.xml";

    private static LoggerContext loggerContext;

    static void reconfigure(String configXml) {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, configXml);
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        loggerContext.reconfigure();
    }

    @BeforeEach
    void before() throws Exception {
        reconfigure(CONFIG_XML);
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    @Test
    void testSimpleDebug() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        assertThat(GelfTestSender.getMessages()).isEmpty();
        logger.debug(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).isEmpty();
    }

    @Test
    void testSimpleInfo() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getFullMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        assertThat(gelfMessage.getLevel()).isEqualTo("6");
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);

        assertThat(gelfMessage.getField("server")).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);
        assertThat(gelfMessage.getField("server.simple")).isEqualTo(RuntimeContainer.HOSTNAME);
        assertThat(gelfMessage.getField("server.fqdn")).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);
        assertThat(gelfMessage.getField("server.addr")).isEqualTo(RuntimeContainer.ADDRESS);

        assertThat(gelfMessage.getField("simpleClassName")).isEqualTo(GelfLogAppenderTests.class.getSimpleName());
    }

    @Test
    void testFqdnHost() throws Exception {

        reconfigure("log4j2/log4j2-origin-host-fqdn.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getHost()).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);
    }

    @Test
    void testSimpleHost() throws Exception {

        reconfigure("log4j2/log4j2-origin-host-simple.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getHost()).isEqualTo(RuntimeContainer.HOSTNAME);
    }

    @Test
    void testCustomHost() throws Exception {

        reconfigure("log4j2/log4j2-origin-host-custom.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getHost()).isEqualTo("my.custom.host");
    }

    @Test
    void testEmptyFacility() throws Exception {

        reconfigure("log4j2/log4j2-empty-facility.xml");
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getFacility()).isEqualTo("");
    }

    @Test
    void testSimpleWarn() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.warn(LOG_MESSAGE);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getLevel()).isEqualTo("4");
    }

    @Test
    void testSimpleError() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.error(LOG_MESSAGE);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getLevel()).isEqualTo("3");
    }

    @Test
    void testSimpleFatal() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.fatal(LOG_MESSAGE);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getLevel()).isEqualTo("2");
    }

    @Test
    void testMDC() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        ThreadContext.put("mdcField1", "my mdc value");
        ThreadContext.put("mdcField2", null);
        ThreadContext.put(GelfUtil.MDC_REQUEST_START_MS, "" + System.currentTimeMillis());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("mdcField1")).isEqualTo("my mdc value");
        assertThat(gelfMessage.getAdditonalFields()).doesNotContainKeys("mdcField2");

        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_DURATION)).isNotNull();
        assertThat(gelfMessage.getField(GelfUtil.MDC_REQUEST_END)).isNotNull();
    }

    @Test
    void testFactory() throws Exception {
        GelfLogAppender result = GelfLogAppender.createAppender(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, false);

        assertThat(result).isNull();

        result = GelfLogAppender.createAppender(null, "name", null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, true);

        assertThat(result).isNull();

        result = GelfLogAppender.createAppender(null, "name", null, null, null, null, null, "host", null, null, null, null, null,
                null, null, null, null, null, null, false);

        assertThat(result).isNotNull();

        result = GelfLogAppender.createAppender(null, "name", null, null, null, null, null, "host", null, null, null, null, null,
                null, "facility", null, null, null, null, false);

        assertThat(result).isNotNull();
    }

    @Test
    void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info((String) null, new IllegalStateException());

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("null");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("null");
    }

    @Test
    void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("", new IllegalStateException("Help!"));

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
        assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
    }

    @Test
    void testEmptyMessage() throws Exception {
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info("");

        assertThat(GelfTestSender.getMessages()).isEmpty();
    }
}
