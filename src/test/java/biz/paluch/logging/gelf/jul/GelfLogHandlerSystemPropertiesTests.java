package biz.paluch.logging.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
class GelfLogHandlerSystemPropertiesTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String PROPERTY1 = "myproperty";
    public static final String PROPERTY1_VALUE = "value of myproperty";

    private static final String PROPERTY2 = "otherproperty";
    public static final String PROPERTY2_VALUE = "value of otherproperty";

    @BeforeEach
    void before() throws Exception {

        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-logging-systemproperties.properties"));
    }

    @Test
    void testDefaults() throws Exception {

        Logger logger = Logger.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getField("propertyField1")).isEqualTo("${user.language}");
        assertThat(gelfMessage.getField("propertyField2")).isEqualTo("${myproperty}");
        assertThat(gelfMessage.getField("propertyField3")).isEqualTo("${otherproperty:fallback}");
        assertThat(gelfMessage.getField("propertyField4")).isEqualTo("embedded${myproperty}property");
    }

}
