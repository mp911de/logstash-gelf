package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch, Daniel Lundsgaard Skovenborg
 */
class GelfLogAppenderNonStringMdcTests {
    private static final String LOG_MESSAGE = "foo bar test log message";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() {
        System.setProperty("log4j2.threadContextMap", "org.apache.logging.log4j.spi.CopyOnWriteSortedArrayThreadContextMap");
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-dynamic-mdc.xml");
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty("log4j2.threadContextMap");
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        loggerContext.reconfigure();
        // Assert that setup worked.
        assert ThreadContext.getThreadContextMap() instanceof ObjectThreadContextMap;
    }

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    /**
     * Copy of GelfLogAppenderDynamicMdcTests#testWithMdcFieldTypes() with raw types instead of String values
     */
    @Test
    void testWithRawMdcFieldTypes() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ObjectThreadContextMap contextMap = (ObjectThreadContextMap) ThreadContext.getThreadContextMap();
        contextMap.putValue("myMdcs", "String");
        contextMap.putValue("myMdcl", 1);
        contextMap.putValue("myMdci", 2L);
        contextMap.putValue("myMdcd", 2.1);
        contextMap.putValue("myMdcf", 2.2d);

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        Map<String, Object> jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcs")).isEqualTo("String");
        assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        assertThat(jsonObject.get("myMdcd")).isEqualTo(2.1);
        assertThat(jsonObject.get("myMdcf")).isEqualTo(2.2);

        ThreadContext.put("myMdcl", "1.1");
        ThreadContext.put("myMdci", "2.1");
        ThreadContext.put("myMdcd", "wrong");
        ThreadContext.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        assertThat(jsonObject.get("myMdcd")).isNull();
        assertThat(jsonObject.get("myMdcf")).isEqualTo(0.0);

        ThreadContext.put("myMdcl", "b");
        ThreadContext.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject.get("myMdcl")).isNull();
        assertThat(jsonObject.get("myMdci")).isEqualTo(0);
    }
}
