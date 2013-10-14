package biz.paluch.logging.gelf;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:16
 */
public interface LogEvent {
    String getMessage();

    Object[] getParameters();

    Throwable getThrowable();

    long getLogTimestamp();

    String getSyslogLevel();

    String getValue(MessageField field);
}
