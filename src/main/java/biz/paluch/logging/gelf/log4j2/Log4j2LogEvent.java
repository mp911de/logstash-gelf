package biz.paluch.logging.gelf.log4j2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.LogEvent;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.MessageField;
import biz.paluch.logging.gelf.Values;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class Log4j2LogEvent implements LogEvent {

    private org.apache.logging.log4j.core.LogEvent logEvent;

    public Log4j2LogEvent(org.apache.logging.log4j.core.LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    @Override
    public String getMessage() {
        return logEvent.getMessage().getFormattedMessage();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return logEvent.getThrown();
    }

    @Override
    public long getLogTimestamp() {
        return logEvent.getTimeMillis();
    }

    @Override
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(logEvent.getLevel());
    }

    private int levelToSyslogLevel(final Level level) {

        switch (level.getStandardLevel()) {
            case FATAL:
                return 2;
            case ERROR:
                return 3;
            case WARN:
                return 4;
            case INFO:
                return 6;
            default:
                return GelfMessage.DEFAUL_LEVEL;

        }
    }

    @Override
    public Values getValues(MessageField field) {

        if (field instanceof MdcMessageField) {
            return new Values(field.getName(), getValue((MdcMessageField) field));
        }

        if (field instanceof PatternLogMessageField) {
            return new Values(field.getName(), getValue((PatternLogMessageField) field));
        }

        if (field instanceof LogMessageField) {
            return new Values(field.getName(), getValues((LogMessageField) field));
        }

        if (field instanceof DynamicMdcMessageField) {
            return getMdcValues((DynamicMdcMessageField) field);
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    public String getValues(LogMessageField field) {
        switch (field.getNamedLogField()) {
            case Severity:
                return logEvent.getLevel().toString();
            case ThreadName:
                return logEvent.getThreadName();
            case SourceClassName:
                return getSourceClassName();
            case SourceLineNumber:
                return getSourceLineNumber();
            case SourceMethodName:
                return getSourceMethodName();
            case SourceSimpleClassName:
                String sourceClassName = getSourceClassName();
                if (sourceClassName == null) {
                    return null;
                }
                return GelfUtil.getSimpleClassName(sourceClassName);
            case LoggerName:
                return logEvent.getLoggerName();
            case Marker:
                if (logEvent.getMarker() != null && !"".equals(logEvent.getMarker().toString())) {
                    return logEvent.getMarker().toString();
                }
                return null;
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    private String getSourceMethodName() {
        if (logEvent.getSource() == null) {
            return null;
        }

        return logEvent.getSource().getMethodName();
    }

    private String getSourceLineNumber() {
        if (logEvent.getSource() == null || logEvent.getSource().getLineNumber() <= 0) {
            return null;
        }

        return "" + logEvent.getSource().getLineNumber();
    }

    private String getSourceClassName() {
        if (logEvent.getSource() == null) {
            return null;
        }

        return logEvent.getSource().getClassName();
    }

    private Values getMdcValues(DynamicMdcMessageField field) {
        Values result = new Values();

        Set<String> mdcNames = getAllMdcNames();
        Set<String> matchingMdcNames = GelfUtil.getMatchingMdcNames(field, mdcNames);

        for (String mdcName : matchingMdcNames) {
            String mdcValue = getMdcValue(mdcName);
            if (mdcName != null) {
                result.setValue(mdcName, mdcValue);
            }
        }

        return result;
    }

    private Set<String> getAllMdcNames() {
        Set<String> mdcNames = new HashSet<String>();

        mdcNames.addAll(logEvent.getContextMap().keySet());
        return mdcNames;
    }

    public String getValue(PatternLogMessageField field) {
        return field.getPatternLayout().toSerializable(logEvent);
    }

    private String getValue(MdcMessageField field) {

        return getMdcValue(field.getMdcName());
    }

    @Override
    public String getMdcValue(String mdcName) {
        Map<String, String> mdcPropertyMap = logEvent.getContextMap();
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
