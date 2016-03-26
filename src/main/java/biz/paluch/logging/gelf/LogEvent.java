package biz.paluch.logging.gelf;

import java.util.Set;

/**
 * 
 * Abstraction for a log event. This interface is usually implemented by a logging framework wrapper to encapsulate the
 * frameworks specifics and expose the required log event details.
 * 
 * @author Mark Paluch
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
     * @param field the field value
     * @return values for the requested field.
     */
    Values getValues(MessageField field);

    /**
     * 
     * @param mdcName Name of the MDC entry.
     * @return one MDC value (or null)
     */
    String getMdcValue(String mdcName);

    /**
     * 
     * @return list of MDC entry names.
     */
    Set<String> getMdcNames();
}
