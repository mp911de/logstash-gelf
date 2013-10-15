package biz.paluch.logging.gelf.log4j2;

import biz.paluch.logging.gelf.LogEvent;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.MessageField;
import org.apache.logging.log4j.Level;

import java.util.Map;

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
        return logEvent.getMillis();
    }

    @Override
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(logEvent.getLevel());
    }

    private int levelToSyslogLevel(final Level level) {
        final int syslogLevel;

        switch (level) {
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
    public String getValue(MessageField field) {

        if (field instanceof MdcMessageField) {
            return getValue((MdcMessageField) field);
        }

        if (field instanceof PatternLogMessageField) {
            return getValue((PatternLogMessageField) field);
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    public String getValue(PatternLogMessageField field) {
        return field.getPatternLayout().toSerializable(logEvent);
    }

    private String getValue(MdcMessageField field) {

        return getMdc(field.getMdcName());
    }

    @Override
    public String getMdc(String mdcName) {
        Map<String, String> mdcPropertyMap = logEvent.getContextMap();
        if (null != mdcPropertyMap && mdcPropertyMap.containsKey(mdcName)) {
            return mdcPropertyMap.get(mdcName);
        }

        return null;
    }
}
