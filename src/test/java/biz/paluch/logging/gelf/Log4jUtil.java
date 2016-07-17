package biz.paluch.logging.gelf;

import org.apache.log4j.helpers.Loader;

/**
 * @author Mark Paluch
 */
public class Log4jUtil {

    /**
     * @return {@literal true} if the Log4j MDC is available
     */
    public static boolean isLog4jMDCAvailable(){
        return !Loader.isJava1();
    }
}
