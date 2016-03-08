package biz.paluch.logging.gelf.log4j2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class GelfLogAppenderDynamicMdcTest {
    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String MDC_MY_MDC = "myMdc";
    public static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    public static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    public static final String VALUE_1 = "value";
    public static final String VALUE_2 = "value1";
    public static final String VALUE_3 = "value2";
    public static final String SOME_FIELD = "someField";
    public static final String SOME_OTHER_FIELD = "someOtherField";

    private static LoggerContext loggerContext;

    @BeforeClass
    public static void setupClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2-dynamic-mdc.xml");
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
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    @Test
    public void testWithoutFields() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(MDC_MY_MDC, VALUE_1);
        ThreadContext.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        ThreadContext.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(VALUE_1, gelfMessage.getField(MDC_MY_MDC));
        assertEquals(VALUE_2, gelfMessage.getField(MY_MDC_WITH_SUFFIX1));
        assertEquals(VALUE_3, gelfMessage.getField(MY_MDC_WITH_SUFFIX2));
    }

    @Test
    public void testWithMdcRegex() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(SOME_FIELD, "included");
        ThreadContext.put(SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField(SOME_FIELD));
        assertNull(gelfMessage.getField(SOME_OTHER_FIELD));
    }

    @Test
    public void testWithMdcFieldTypes() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put("myMdcs", "String");
        ThreadContext.put("myMdcl", "1");
        ThreadContext.put("myMdci", "2");
        ThreadContext.put("myMdcd", "2.1");
        ThreadContext.put("myMdcf", "2.2");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        JSONObject jsonObject = (JSONObject) JSONValue.parse(gelfMessage.toJson(""));

        assertEquals("String", jsonObject.get("myMdcs"));
        assertEquals(1L, jsonObject.get("myMdcl"));
        assertEquals(2L, jsonObject.get("myMdci"));

        assertEquals(2.1, jsonObject.get("myMdcd"));
        assertEquals(2.2, jsonObject.get("myMdcf"));

        ThreadContext.put("myMdcl", "1.1");
        ThreadContext.put("myMdci", "2.1");
        ThreadContext.put("myMdcd", "wrong");
        ThreadContext.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = (JSONObject) JSONValue.parse(gelfMessage.toJson(""));

        assertEquals(1L, jsonObject.get("myMdcl"));
        assertEquals(2L, jsonObject.get("myMdci"));

        assertNull(jsonObject.get("myMdcd"));
        assertEquals(0.0, jsonObject.get("myMdcf"));

        ThreadContext.put("myMdcl", "b");
        ThreadContext.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = (JSONObject) JSONValue.parse(gelfMessage.toJson(""));

        assertNull(jsonObject.get("myMdcl"));
        assertEquals(0L, jsonObject.get("myMdci"));
    }
}
