package biz.paluch.logging.gelf.log4j2;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.LogEvent;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.MessageField;
import biz.paluch.logging.gelf.Values;
import org.apache.logging.log4j.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
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
        final int syslogLevel;

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
                return 7;

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

        if (field instanceof DynamicMdcMessageField) {
            return getMdcValues((DynamicMdcMessageField) field);
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
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
