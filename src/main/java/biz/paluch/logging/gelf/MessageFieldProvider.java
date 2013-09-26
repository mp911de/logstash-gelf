package biz.paluch.logging.gelf;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:16
 */
public interface MessageFieldProvider
{
    String getMessage();

    Object[] getParameters();

    String getThreadName();

    Throwable getThrowable();

    long getLogTimestamp();

    String getLevelName();

    String getSyslogLevel();

    String getSourceClassName();

    String getSourceMethodName();
}
