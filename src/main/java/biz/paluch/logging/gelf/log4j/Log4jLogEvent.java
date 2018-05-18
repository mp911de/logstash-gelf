package biz.paluch.logging.gelf.log4j;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 26.09.13 15:37
 */
class Log4jLogEvent implements LogEvent {

    private static final Field mdcCopy = getMdcCopyField();

    private final LoggingEvent loggingEvent;
    private final Map mdc;

    public Log4jLogEvent(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
        this.mdc = getMdc(loggingEvent);
    }

    private Map getMdc(LoggingEvent loggingEvent) {
        Map mdc = null;

        try {
            if (mdcCopy != null) {
                mdc = (Map) mdcCopy.get(loggingEvent);
            }
        } catch (IllegalAccessException e) {
        }

        if (mdc == null) {
            mdc = MDC.getContext();
        }
        return mdc;
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
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(loggingEvent.getLevel());
    }

    private int levelToSyslogLevel(final Level level) {
        if (level.toInt() <= Level.DEBUG.toInt()) {
            return GelfMessage.DEFAUL_LEVEL;
        }

        if (level.toInt() <= Level.INFO.toInt()) {
            return 6;
        }

        if (level.toInt() <= Level.WARN.toInt()) {
            return 4;
        }

        if (level.toInt() <= Level.ERROR.toInt()) {
            return 3;
        }

        if (level.toInt() <= Level.FATAL.toInt()) {
            return 2;
        }

        if (level.toInt() > Level.FATAL.toInt()) {
            return 0;
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
            case SourceLineNumber:
                return getSourceLineNumber();
            case SourceMethodName:
                return getSourceMethodName();
            case SourceSimpleClassName:
                String sourceClassName = getSourceClassName();
                if(sourceClassName == null){
                    return null;
                }
                return GelfUtil.getSimpleClassName(sourceClassName);
            case LoggerName:
                return loggingEvent.getLoggerName();
            case NDC:
                String ndc = loggingEvent.getNDC();
                if (ndc != null && !"".equals(ndc)) {
                    return ndc;
                }
                return null;
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    private String getSourceMethodName() {
        String methodName = loggingEvent.getLocationInformation().getMethodName();
        if(LocationInfo.NA.equals(methodName)){
            return null;
        }
        return methodName;
    }

    private String getSourceLineNumber() {
        String lineNumber = loggingEvent.getLocationInformation().getLineNumber();
        if(LocationInfo.NA.equals(lineNumber)){
            return null;
        }
        return lineNumber;
    }

    private String getSourceClassName() {
        String className = loggingEvent.getLocationInformation().getClassName();
        if(LocationInfo.NA.equals(className)){
            return null;
        }
        return className;
    }

    private String getValue(MdcMessageField field) {

        return getMdcValue(field.getMdcName());
    }

    @Override
    public String getMdcValue(String mdcName) {

        if (mdc != null) {
            Object value = mdc.get(mdcName);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private Values getMdcValues(DynamicMdcMessageField field) {
        Values result = new Values();

        Set<String> mdcNames = getAllMdcNames();
        Set<String> matchingMdcNames = getMatchingMdcNames(field, mdcNames);

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

        if (mdc != null) {
            mdcNames.addAll(mdc.keySet());
        }
        return mdcNames;
    }

    private Set<String> getMatchingMdcNames(DynamicMdcMessageField field, Set<String> mdcNames) {
        Set<String> matchingMdcNames = new HashSet<String>();

        for (String mdcName : mdcNames) {
            if (field.getPattern().matcher(mdcName).matches()) {
                matchingMdcNames.add(mdcName);
            }
        }
        return matchingMdcNames;
    }

    @Override
    public Set<String> getMdcNames() {
        return getAllMdcNames();
    }

    private static Field getMdcCopyField() {

        try {
            Field mdcCopy = LoggingEvent.class.getDeclaredField("mdcCopy");
            mdcCopy.setAccessible(true);

            return mdcCopy;
        } catch (Exception e) {
            return null;
        }
    }
}
