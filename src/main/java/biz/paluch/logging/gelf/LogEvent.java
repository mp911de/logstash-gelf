package biz.paluch.logging.gelf;

import java.util.Set;

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
