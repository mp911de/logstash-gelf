package biz.paluch.logging.gelf.logback;

import java.util.Map;

import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.LogEvent;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.MessageField;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
class LogbackLogEvent implements LogEvent {

    private ILoggingEvent loggingEvent;

    public LogbackLogEvent(ILoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
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
    public String getSyslogLevel() {
        return levelToSyslogLevel(loggingEvent.getLevel());
    }

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

    @Override
    public String getValue(MessageField field) {
        if (field instanceof LogMessageField) {
            return getValue((LogMessageField) field);
        }

        if (field instanceof MdcMessageField) {
            return getValue((MdcMessageField) field);
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    public String getValue(LogMessageField field) {

        switch (field.getNamedLogField()) {
            case Severity:
                return loggingEvent.getLevel().toString();
            case ThreadName:
                return loggingEvent.getThreadName();
            case SourceClassName:
                return getSourceClassName();
            case SourceMethodName:
                return getSourceMethodName();
            case SourceSimpleClassName:
                return GelfUtil.getSimpleClassName(getSourceClassName());
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    private String getValue(MdcMessageField field) {

        return getMdc(field.getMdcName());
    }

    @Override
    public String getMdc(String mdcName) {
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
        if (null != mdcPropertyMap && mdcPropertyMap.containsKey(mdcName)) {
            return mdcPropertyMap.get(mdcName);
        }

        return null;
    }
}
