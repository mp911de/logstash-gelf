package biz.paluch.logging.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * @author Mark Paluch
 */
public class GelfLogAppenderPropagateExceptionsTests {

    public static final String LOG_MESSAGE = "foo bar test log message";
    public static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    private static LoggerContext loggerContext;

    @BeforeAll
    public static void beforeAll() throws Exception {
        loggerContext = Configurator.initialize("GelfLogAppenderPropagateExceptionsTests",
                GelfLogAppenderPropagateExceptionsTests.class.getClassLoader(), "log4j2/log4j2-propagate-exceptions.xml");
    }

    @AfterAll
    public static void afterAll() throws Exception {
        Configurator.shutdown(loggerContext);
    }

    @Test
    public void shouldPropagateException() throws Exception {

        assertThrows(AppenderLoggingException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                Logger logger = loggerContext.getLogger("biz.exception");
                logger.info(LOG_MESSAGE);
            }
        });
    }

    @Test
    public void shouldUseFailoverAppender() throws Exception {

        Logger logger = loggerContext.getLogger("biz.failover");
        logger.info(LOG_MESSAGE);

        ListAppender failoverList = getListAppender("failoverList");
        assertThat(failoverList.getEvents()).hasSize(1);
    }

    @Test
    public void shouldIgnoreException() throws Exception {

        Logger logger = loggerContext.getLogger("biz.ignore");
        logger.info(LOG_MESSAGE);

        ListAppender ignoreList = getListAppender("ignoreList");
        assertThat(ignoreList.getEvents()).hasSize(1);
    }

    public ListAppender getListAppender(String name) {
        return (ListAppender) loggerContext.getConfiguration().getAppenders().get(name);
    }

}
