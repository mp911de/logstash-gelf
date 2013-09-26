package biz.paluch.logging.gelf.log4j;

import java.util.Collection;

import biz.paluch.logging.gelf.MessageFieldProvider;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:37
 */
public class Log4jLoggingEventProvider implements MessageFieldProvider
{
    private LoggingEvent loggingEvent;

    public Log4jLoggingEventProvider(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    @Override
    public String getMessage() {
        return loggingEvent.getRenderedMessage();
    }

    @Override
    public Object[] getParameters() {
        Collection collection = loggingEvent.getProperties().values();
        return collection.toArray(new Object[collection.size()]);
    }

    @Override
    public String getThreadName() {
        return loggingEvent.getThreadName();
    }

    @Override
    public Throwable getThrowable() {
        ThrowableInformation ti = loggingEvent.getThrowableInformation();
        if (ti != null) {
            return ti.getThrowable();
        }

        return null;
    }

    @Override
    public long getLogTimestamp() {
        return Log4jVersionChecker.getTimeStamp(loggingEvent);
    }

    @Override
    public String getLevelName() {
        return loggingEvent.getLevel().toString();
    }

    @Override
    public String getSyslogLevel() {
        return null;
    }

    @Override
    public String getSourceClassName() {

        LocationInfo locationInfo = loggingEvent.getLocationInformation();
        if (locationInfo != null) {
            return locationInfo.getClassName();
        }
        return "";
    }

    @Override
    public String getSourceMethodName() {
        LocationInfo locationInfo = loggingEvent.getLocationInformation();
        if (locationInfo != null) {
            return locationInfo.getMethodName();
        }
        return "";
    }
}
