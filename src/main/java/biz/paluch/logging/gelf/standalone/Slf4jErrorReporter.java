package biz.paluch.logging.gelf.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.paluch.logging.gelf.intern.ErrorReporter;

/**
 * @author Mark Paluch
 * @since 21.07.14 17:34
 */
public class Slf4jErrorReporter implements ErrorReporter {
    private Logger logger;

    public Slf4jErrorReporter() {
        logger = LoggerFactory.getLogger(getClass());
    }

    public Slf4jErrorReporter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void reportError(String message, Exception e) {
        logger.warn(message, e);
    }
}
