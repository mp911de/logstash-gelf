package biz.paluch.logging.gelf.wildfly;

import static biz.paluch.logging.gelf.wildfly.WildFlyLogTestUtil.getWildFlyGelfLogHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.logmanager.MDC;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 11.08.14 08:36
 */
public class WildFlyGelfLogHandlerDynamicMdcTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    public static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    public static final String MDC_SOME_FIELD = "someField";
    public static final String MDC_SOME_OTHER_FIELD = "someOtherField";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";
    public static final String VALUE_3 = "value3";
    public static final String MDC_MY_MDC = "myMdc";

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
        MDC.clear();
    }

    @Test
    public void testWithoutFields() throws Exception {

        WildFlyGelfLogHandler handler = getLogHandler();
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        assertNull(myMdc);
    }

    @Test
    public void testWithMdcPrefix() throws Exception {

        WildFlyGelfLogHandler handler = getLogHandler();
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put(MDC_MY_MDC, VALUE_1);
        MDC.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        MDC.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(VALUE_1, gelfMessage.getField(MDC_MY_MDC));
        assertEquals(VALUE_2, gelfMessage.getField(MY_MDC_WITH_SUFFIX1));
        assertEquals(VALUE_3, gelfMessage.getField(MY_MDC_WITH_SUFFIX2));

    }

    @Test
    public void testWithMdcRegex() throws Exception {

        WildFlyGelfLogHandler handler = getLogHandler();
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);
        MDC.put(MDC_SOME_FIELD, "included");
        MDC.put(MDC_SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals("included", gelfMessage.getField(MDC_SOME_FIELD));
        assertNull(gelfMessage.getField("someOtherField"));

    }

    @Test
    public void testWithDifferentMDCsPrefix() throws Exception {

        WildFlyGelfLogHandler handler = getLogHandler();

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put(MDC_MY_MDC, VALUE_1);
        MDC.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        MDC.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertEquals(VALUE_1, gelfMessage.getField(MDC_MY_MDC));
        assertEquals(VALUE_2, gelfMessage.getField(MY_MDC_WITH_SUFFIX1));
        assertEquals(VALUE_3, gelfMessage.getField(MY_MDC_WITH_SUFFIX2));

    }

    @Test
    public void testWithMdcFieldTypes() throws Exception {

        WildFlyGelfLogHandler handler = getLogHandler();
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put("myMdcs", "String");
        MDC.put("myMdcl", "1");
        MDC.put("myMdci", "2");
        MDC.put("myMdcd", "2.1");
        MDC.put("myMdcf", "2.2");

        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        Map<String, Object> jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertEquals("String", jsonObject.get("myMdcs"));
        assertEquals(1, jsonObject.get("myMdcl"));
        assertEquals(2, jsonObject.get("myMdci"));

        assertEquals(2.1, jsonObject.get("myMdcd"));
        assertEquals(2.2, jsonObject.get("myMdcf"));

        MDC.put("myMdcl", "1.1");
        MDC.put("myMdci", "2.1");
        MDC.put("myMdcd", "wrong");
        MDC.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertEquals(1, jsonObject.get("myMdcl"));
        assertEquals(2, jsonObject.get("myMdci"));

        assertNull(jsonObject.get("myMdcd"));
        assertEquals(0.0, jsonObject.get("myMdcf"));

        MDC.put("myMdcl", "b");
        MDC.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        assertEquals(1, GelfTestSender.getMessages().size());

        gelfMessage = GelfTestSender.getMessages().get(0);
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        assertNull(jsonObject.get("myMdcl"));
        assertEquals(0, jsonObject.get("myMdci"));
    }

    private WildFlyGelfLogHandler getLogHandler() {
        WildFlyGelfLogHandler handler = getWildFlyGelfLogHandler();
        handler.setDynamicMdcFields("myMdc.*,[a-z]+Field");
        handler.setAdditionalFieldTypes("myMdcs=String,myMdci=long,myMdcl=Long,myMdcf=double,myMdcd=Double");

        return handler;
    }
}
