package biz.paluch.logging.gelf.log4j;

import org.apache.log4j.helpers.Loader;

/**
 * @author Mark Paluch
 */
class Log4jUtil {

    /**
     * @return {@literal true} if the Log4j MDC is available
     */
    public static boolean isLog4jMDCAvailable() {
        return !Loader.isJava1();
    }
}
