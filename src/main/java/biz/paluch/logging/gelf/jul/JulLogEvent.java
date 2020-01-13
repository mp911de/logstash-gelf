package biz.paluch.logging.gelf.jul;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * {@link LogEvent} implementation for a Java Util Logging {@link LogRecord}.
 *
 * @author Mark Paluch
 * @author Loïc Mathieu
 * @author Ralf Thaenert
 * @since 26.09.13 15:22
 */
public class JulLogEvent implements LogEvent {

    private static Map<String, String> threadNameCache = new ConcurrentHashMap<>();

    private LogRecord logRecord;

    public JulLogEvent(LogRecord logRecord) {
        this.logRecord = logRecord;
    }

    @Override
    public String getMessage() {
        return createMessage(logRecord);
    }

    @Override
    public Object[] getParameters() {
        return logRecord.getParameters();
    }

    @Override
    public Throwable getThrowable() {
        return logRecord.getThrown();
    }

    @Override
    public long getLogTimestamp() {
        return logRecord.getMillis();
    }

    @Override
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(logRecord.getLevel());
    }

    private String createMessage(LogRecord record) {
        String message = record.getMessage();
        Object[] parameters = record.getParameters();

        if (message == null) {
            message = "";
        }

        if (record.getResourceBundle() != null && record.getResourceBundle().containsKey(message)) {
            message = record.getResourceBundle().getString(message);
        }

        if (parameters != null && parameters.length > 0) {
            String originalMessage = message;

            // by default, using {0}, {1}, etc. -> MessageFormat
            if (message.indexOf('{') != -1) {
                try {
                    message = MessageFormat.format(message, parameters);
                } catch (IllegalArgumentException e) {
                    // leaving message as it is to avoid compatibility problems
                    message = record.getMessage();
                } catch (NullPointerException e) {
                    // ignore
                }
            }

            if (message.equals(originalMessage)) {
                // if the text is the same, assuming this is String.format type log (%s, %d, etc.)
                try {
                    message = String.format(message, parameters);
                } catch (IllegalFormatException e) {
                    // leaving message as it is to avoid compatibility problems
                    message = record.getMessage();
                } catch (NullPointerException e) {
                    // ignore
                }
            }
        }
        return message;
    }

    private String getThreadName(LogRecord record) {

        String cacheKey = "" + record.getThreadID();
        if (threadNameCache.containsKey(cacheKey)) {
            return threadNameCache.get(cacheKey);
        }

        long threadId = record.getThreadID();
        String threadName = cacheKey;

        Thread currentThread = Thread.currentThread();
        if (record.getThreadID() == currentThread.getId()) {
            threadName = currentThread.getName();
        } else {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            for (Thread thread : threadSet) {
                if (thread.getId() == threadId) {
                    threadName = thread.getName();
                    break;
                }
            }
        }

        threadNameCache.put(cacheKey, threadName);

        return threadName;
    }

    private int levelToSyslogLevel(final Level level) {

        if (level.intValue() <= Level.CONFIG.intValue()) {
            return GelfMessage.DEFAUL_LEVEL;
        }

        if (level.intValue() <= Level.INFO.intValue()) {
            return 6;
        }

        if (level.intValue() <= Level.WARNING.intValue()) {
            return 4;
        }

        if (level.intValue() <= Level.SEVERE.intValue()) {
            return 3;
        }

        if (level.intValue() > Level.SEVERE.intValue()) {
            return 2;
        }

        return GelfMessage.DEFAUL_LEVEL;
    }

    public Values getValues(MessageField field) {
        if (field instanceof LogMessageField) {
            return new Values(field.getName(), getValue((LogMessageField) field));
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    public String getValue(LogMessageField field) {

        switch (field.getNamedLogField()) {
            case Severity:
                return logRecord.getLevel().getName();
            case ThreadName:
                return getThreadName(logRecord);
            case SourceClassName:
                return getSourceClassName();
            case SourceMethodName:
                return getSourceMethodName();
            case SourceSimpleClassName:
                return GelfUtil.getSimpleClassName(logRecord.getSourceClassName());
            case LoggerName:
                return logRecord.getLoggerName();
        }

        throw new UnsupportedOperationException("Cannot provide value for " + field);
    }

    private String getSourceMethodName() {
        String sourceMethodName = logRecord.getSourceMethodName();
        if (sourceMethodName == null || "<unknown>".equals(sourceMethodName)) {
            return null;
        }

        return sourceMethodName;
    }

    private String getSourceClassName() {
        String sourceClassName = logRecord.getSourceClassName();
        if (sourceClassName == null || "<unknown>".equals(sourceClassName)) {
            return null;
        }

        return sourceClassName;
    }

    @Override
    public String getMdcValue(String mdcName) {
        return null;
    }

    @Override
    public Set<String> getMdcNames() {
        return Collections.emptySet();
    }
}
