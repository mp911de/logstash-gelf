package biz.paluch.logging.gelf.jul;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.util.IllegalFormatConversionException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import biz.paluch.logging.gelf.LogEvent;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:22
 */
public class JulLogEvent implements LogEvent {
    private static Map<String, String> threadNameCache = new ConcurrentHashMap<String, String>();

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
    public String getThreadName() {
        return getThreadName(logRecord);
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
    public String getLevelName() {
        return logRecord.getLevel().getName();
    }

    @Override
    public String getSyslogLevel() {
        return "" + levelToSyslogLevel(logRecord.getLevel());
    }

    @Override
    public String getSourceClassName() {
        return logRecord.getSourceClassName();
    }

    @Override
    public String getSourceMethodName() {
        return logRecord.getSourceMethodName();
    }

    private String createMessage(LogRecord record) {
        String message = record.getMessage();
        Object[] parameters = record.getParameters();

        if (message == null) {
            message = "";
        }
        if (parameters != null && parameters.length > 0) {
            // by default, using {0}, {1}, etc. -> MessageFormat

            try {
                message = MessageFormat.format(message, parameters);
            } catch (IllegalFormatConversionException e) {
                // leaving message as it is to avoid compatibility problems
                message = record.getMessage();
            } catch (NullPointerException e) {
                // ignore
            }

            if (message.equals(record.getMessage())) {
                // if the text is the same, assuming this is String.format type log (%s, %d, etc.)
                try {
                    message = String.format(message, parameters);
                } catch (IllegalFormatConversionException e) {
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
        String threadName = "" + record.getThreadID();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long id : threadMXBean.getAllThreadIds()) {
            if (id == threadId) {
                ThreadInfo threadInfo = threadMXBean.getThreadInfo(id);
                if (threadInfo != null) {
                    threadName = threadInfo.getThreadName();
                }
                break;
            }
        }

        threadNameCache.put(cacheKey, threadName);

        return threadName;
    }

    private int levelToSyslogLevel(final Level level) {
        final int syslogLevel;
        if (level == Level.SEVERE) {
            syslogLevel = 3;
        } else if (level == Level.WARNING) {
            syslogLevel = 4;
        } else if (level == Level.INFO) {
            syslogLevel = 6;
        } else {
            syslogLevel = 7;
        }
        return syslogLevel;
    }

}
