package biz.paluch.logging.gelf.log4j2;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.junit.InitialLoggerContext;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderPropagateExceptionsTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    @ClassRule
    public static final InitialLoggerContext loggerContext = new InitialLoggerContext(
            "log4j2/log4j2-propagate-exceptions.xml");


    @Before
    public void before() throws Exception {
        ThreadContext.clearAll();
    }

    @Test(expected = AppenderLoggingException.class)
    public void shouldPropagateException() throws Exception {

        Logger logger = loggerContext.getLogger("biz.exception");
        logger.info(LOG_MESSAGE);
    }

    @Test
    public void shouldUseFailoverAppender() throws Exception {

        Logger logger = loggerContext.getLogger("biz.failover");
        logger.info(LOG_MESSAGE);

        ListAppender failoverList = loggerContext.getListAppender("failoverList");
        assertThat(failoverList.getEvents()).hasSize(1);
    }

    @Test
    public void shouldIgnoreException() throws Exception {

        Logger logger = loggerContext.getLogger("biz.ignore");
        logger.info(LOG_MESSAGE);

        ListAppender ignoreList = loggerContext.getListAppender("ignoreList");
        assertThat(ignoreList.getEvents()).hasSize(1);
    }

}
