package biz.paluch.logging.gelf;

/**
 * 
 * Generic Log Event.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:16
 */
public interface LogEvent {

    /**
     * 
     * @return the rendered message.
     */
    String getMessage();

    /**
     * 
     * @return array of parameters, if available, else empty object array.
     */
    Object[] getParameters();

    /**
     * 
     * @return throwable, if available, else null.
     */
    Throwable getThrowable();

    /**
     * 
     * @return timestamp of the log event.
     */
    long getLogTimestamp();

    /**
     * 
     * @return numeric syslog level.
     */
    String getSyslogLevel();

    /**
     * 
     * @param field
     * @return value (or null) for the requested field.
     */
    String getValue(MessageField field);

    /**
     * 
     * @param mdcName
     * @return MDC value, if available, else null.
     */
    String getMdc(String mdcName);

}
