package biz.paluch.logging.gelf.logback;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.GelfMessage;
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
        return loggingEvent.getFormattedMessage();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        Throwable result = null;
        IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
        if (throwableProxy instanceof ThrowableProxy) {
            result = ((ThrowableProxy) throwableProxy).getThrowable();
        }
        return result;
    }

    @Override
    public long getLogTimestamp() {
        return loggingEvent.getTimeStamp();
    }

    @Override
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(loggingEvent.getLevel());
    }

    public String getSourceClassName() {
        StackTraceElement calleeStackTraceElement = getCalleeStackTraceElement();
        if (null == calleeStackTraceElement) {
            return null;
        }

        return calleeStackTraceElement.getClassName();
    }

    private StackTraceElement getCalleeStackTraceElement() {
        StackTraceElement[] callerData = loggingEvent.getCallerData();

        if (null != callerData && callerData.length > 0) {
            return callerData[0];
        } else {
            return null;
        }
    }

    public String getSourceMethodName() {
        StackTraceElement calleeStackTraceElement = getCalleeStackTraceElement();
        if (null == calleeStackTraceElement) {
            return null;
        }

        return calleeStackTraceElement.getMethodName();
    }

    public String getSourceLine() {
        StackTraceElement calleeStackTraceElement = getCalleeStackTraceElement();
        if (null == calleeStackTraceElement) {
            return null;
        }

        return "" + calleeStackTraceElement.getLineNumber();
    }

    private int levelToSyslogLevel(final Level level) {

        int intLevel = level.toInt();

        if (intLevel <= Level.DEBUG_INT) {
            return GelfMessage.DEFAUL_LEVEL;
        }

        if (intLevel <= Level.INFO_INT) {
            return 6;
        }

        if (intLevel <= Level.WARN_INT) {
            return 4;
        }

        if (intLevel <= Level.ERROR_INT) {
            return 3;
        }

        if (intLevel < Level.ERROR_INT) {
            return 2;
        }

        return GelfMessage.DEFAUL_LEVEL;
    }

    @Override
    public Values getValues(MessageField field) {
        if (field instanceof LogMessageField) {
            return new Values(field.getName(), getValue((LogMessageField) field));
        }

        if (field instanceof MdcMessageField) {
            return new Values(field.getName(), getValue((MdcMessageField) field));
        }

        if (field instanceof DynamicMdcMessageField) {
            return getMdcValues((DynamicMdcMessageField) field);
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
            case SourceLineNumber:
                return getSourceLine();
            case SourceSimpleClassName:
                String sourceClassName = getSourceClassName();
                if (sourceClassName == null) {
                    return null;
                }
                return GelfUtil.getSimpleClassName(getSourceClassName());
            case LoggerName:
                return loggingEvent.getLoggerName();
            case Marker:
                if (loggingEvent.getMarker() != null && !"".equals(loggingEvent.getMarker().toString())) {
                    return loggingEvent.getMarker().toString();
                }
                return null;
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    private Values getMdcValues(DynamicMdcMessageField field) {
        Values result = new Values();

        Set<String> mdcNames = getAllMdcNames();
        Set<String> matchingMdcNames = GelfUtil.getMatchingMdcNames(field, mdcNames);

        for (String mdcName : matchingMdcNames) {
            String mdcValue = getMdcValue(mdcName);
            if (mdcValue != null) {
                result.setValue(mdcName, mdcValue);
            }
        }

        return result;
    }

    private Set<String> getAllMdcNames() {
        Set<String> mdcNames = new HashSet<>();

        mdcNames.addAll(loggingEvent.getMDCPropertyMap().keySet());
        return mdcNames;
    }

    private String getValue(MdcMessageField field) {
        return getMdcValue(field.getMdcName());
    }

    @Override
    public String getMdcValue(String mdcName) {
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
        if (null != mdcPropertyMap && mdcPropertyMap.containsKey(mdcName)) {
            return mdcPropertyMap.get(mdcName);
        }

        return null;
    }

    @Override
    public Set<String> getMdcNames() {
        return getAllMdcNames();
    }
}
