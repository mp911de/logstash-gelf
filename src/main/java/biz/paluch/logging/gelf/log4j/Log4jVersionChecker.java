package biz.paluch.logging.gelf.log4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Mark Paluch
 */
class Log4jVersionChecker {

    private static Method methodGetTimeStamp = null;

    static {
        Method[] declaredMethods = LoggingEvent.class.getDeclaredMethods();
        for (Method m : declaredMethods) {
            if (m.getName().equals("getTimeStamp")) {
                methodGetTimeStamp = m;
                break;
            }
        }
    }

    private Log4jVersionChecker() {
        // no instance allowed
    }

    public static long getTimeStamp(LoggingEvent event) {

        long timeStamp = 0;
        if (methodGetTimeStamp != null) {

            try {
                timeStamp = (Long) methodGetTimeStamp.invoke(event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                // Just return the current timestamp
            }
        }

        return timeStamp == 0 ? System.currentTimeMillis() : timeStamp;
    }
}
