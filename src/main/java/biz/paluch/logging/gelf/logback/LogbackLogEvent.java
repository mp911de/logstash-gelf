package biz.paluch.logging.gelf.logback;

import java.util.Map;

import biz.paluch.logging.gelf.MdcLogEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
public class LogbackLogEvent implements MdcLogEvent {

    private ILoggingEvent loggingEvent;

    public LogbackLogEvent(ILoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    @Override
    public Object getMDC(String item) {
        Object result = null;
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
        if (null != mdcPropertyMap && mdcPropertyMap.containsKey(item)) {
            result = mdcPropertyMap.get(item);
        }
        return result;
    }

    @Override
    public String getMessage() {
        return loggingEvent.getMessage();
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
        Throwable result = null;
        IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
        if (null != throwableProxy) {
            if (throwableProxy instanceof ThrowableProxy) {
                result = ((ThrowableProxy) throwableProxy).getThrowable();
            }
        }
        return result;
    }

    @Override
    public long getLogTimestamp() {
        return loggingEvent.getTimeStamp();
    }

    @Override
    public String getLevelName() {
        String result = "";
        Level loggingEventLevel = loggingEvent.getLevel();
        if (null != loggingEventLevel) {
            result = loggingEventLevel.toString();
        }
        return result;
    }

    @Override
    public String getSyslogLevel() {
        return levelToSyslogLevel(loggingEvent.getLevel());
    }

    @Override
    public String getSourceClassName() {
        StackTraceElement calleeStackTraceElement = getCalleeStackTraceElement();
        if (null == calleeStackTraceElement) {
            return "";
        }

        return calleeStackTraceElement.getClassName();
    }

    private StackTraceElement getCalleeStackTraceElement() {
        StackTraceElement[] callerData = loggingEvent.getCallerData();

        if (null != callerData) {
            return callerData[0];
        } else {
            return null;
        }
    }

    @Override
    public String getSourceMethodName() {
        StackTraceElement calleeStackTraceElement = getCalleeStackTraceElement();
        if (null == calleeStackTraceElement) {
            return "";
        }

        return calleeStackTraceElement.getMethodName();
    }

    private String levelToSyslogLevel(final Level level) {
        String result = "7";

        int intLevel = level.toInt();
        if (intLevel > Level.ERROR_INT) {
            result = "2";
        } else if (intLevel == Level.ERROR_INT) {
            result = "3";
        } else if (intLevel == Level.WARN_INT) {
            result = "4";
        } else if (intLevel == Level.INFO_INT) {
            result = "6";
        }
        return result;
    }

}
