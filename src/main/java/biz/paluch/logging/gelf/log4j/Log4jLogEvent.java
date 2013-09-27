package biz.paluch.logging.gelf.log4j;

import biz.paluch.logging.gelf.MdcLogEvent;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Collection;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:37
 */
public class Log4jLogEvent implements MdcLogEvent {
    private LoggingEvent loggingEvent;

    public Log4jLogEvent(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    @Override
    public String getMessage() {
        return loggingEvent.getRenderedMessage();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
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
        return "" + levelToSyslogLevel(loggingEvent.getLevel());
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

    @Override
    public Object getMDC(String item) {
        return loggingEvent.getMDC(item);
    }

    private int levelToSyslogLevel(final Level level) {
        final int syslogLevel;

        switch (level.toInt()) {
            case Level.FATAL_INT:
                return 2;
            case Level.ERROR_INT:
                return 3;
            case Level.WARN_INT:
                return 4;
            case Level.INFO_INT:
                return 6;
            default:
                return 7;

        }
    }
}
