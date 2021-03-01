package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Daniel Lundsgaard Skovenborg
 */
class GelfLogAppenderNonStringMdcTests {

    private static final String LOG_MESSAGE = "foo bar test log message";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() {
        System.setProperty("log4j2.threadContextMap", "org.apache.logging.log4j.spi.CopyOnWriteSortedArrayThreadContextMap");
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-dynamic-mdc.xml");
        PropertiesUtil.getProperties().reload();
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty("log4j2.threadContextMap");
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        PropertiesUtil.getProperties().reload();
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

        assertThat(jsonObject).containsEntry("myMdcs", "String");
        assertThat(jsonObject).containsEntry("myMdcl", 1);
        assertThat(jsonObject).containsEntry("myMdci", 2);

        assertThat(jsonObject).containsEntry("myMdcd", 2.1);
        assertThat(jsonObject).containsEntry("myMdcf", 2.2);

        ThreadContext.put("myMdcl", "1.1");
        ThreadContext.put("myMdci", "2.1");
        ThreadContext.put("myMdcd", "wrong");
        ThreadContext.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject).containsEntry("myMdcl", 1);
        assertThat(jsonObject).containsEntry("myMdci", 2);

        assertThat(jsonObject).doesNotContainKey("myMdcd");
        assertThat(jsonObject).containsEntry("myMdcf", 0.0);

        ThreadContext.put("myMdcl", "b");
        ThreadContext.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertThat(jsonObject).doesNotContainKey("myMdcl");
        assertThat(jsonObject).containsEntry("myMdci", 0);
    }

}
