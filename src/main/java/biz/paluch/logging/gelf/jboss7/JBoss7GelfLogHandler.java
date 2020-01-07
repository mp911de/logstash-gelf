package biz.paluch.logging.gelf.jboss7;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.*;

import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;

import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.errormanager.OnlyOnceErrorManager;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Java-Util-Logging Handler creates GELF Messages and posts
 * them using UDP (default) or TCP. Following parameters are supported/needed:
 * <ul>
 * <li>host (Mandatory): Hostname/IP-Address of the Logstash Host
 * <ul>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * <li>See docs for more details</li>
 * </ul>
 * </li>
 * <li>enabled (Optional): Whether this handler is enabled, default true</li>
 * <li>port (Optional): Port, default 12201</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field (true/false/throwable reference [0 = throwable, 1 =
 * throwable.cause, -1 = root cause]), default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>includeLogMessageParameters (Optional): Include message parameters from the log event (see
 * {@link LogRecord#getParameters()}, default true</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>level (Optional): Log-Level, default INFO</li>
 * <li>filter (Optional): Class-Name of a Log-Filter, default none</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg.
 * .GelfLogHandler.additionalFields=fieldName=Value,field2=value2</li>
 * <li>additionalFieldTypes (Optional): Type specification for additional and MDC fields. Supported types: String, long, Long,
 * double, Double and discover (default if not specified, discover field type on parseability). Eg. field=String,field2=double</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * .JBoss7GelfLogHandler.mdcFields=Application,Version,SomeOtherFieldName</li>
 * <li>dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular
 * expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name.
 * .JBoss7GelfLogHandler.dynamicMdcFields=mdc.*,[mdc|MDC]fields</li>
 * <li>dynamicMdcFieldTypes (Optional): Pattern-based type specification for additional and MDC fields. Key-value pairs are
 * comma-separated. Supported types: String, long, Long, double, Double. Eg. my_field.*=String,business\..*\.field=double</li>
 * <li>includeFullMdc (Optional): Include all fields from the MDC, default false</li>
 * </ul>
 * <a name="mdcProfiling"></a> <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must
 * set one value in the MDC:
 * <ul>
 * <li>profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)</li>
 * </ul>
 * <p>
 * Two values are set by the Log Appender:
 * </p>
 * <ul>
 * <li>profiling.requestEnd: End-Time of the Request-End in Date.toString-representation</li>
 * <li>profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)</li>
 * </ul>
 *
 * The {@link #publish(LogRecord)} method is thread-safe and may be called by different threads at any time.
 *
 * @author Mark Paluch
 */
public class JBoss7GelfLogHandler extends biz.paluch.logging.gelf.jul.GelfLogHandler {

    private static final ErrorManager DEFAULT_ERROR_MANAGER = new OnlyOnceErrorManager();
    private boolean enabled = true;

    public JBoss7GelfLogHandler() {
        super();
        super.setErrorManager(DEFAULT_ERROR_MANAGER);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Manually enable/disable the handler. This is also called by wildfly logger setup routines on server-startup with the
     * value of the "enabled" attribute of {@code <custom-handler>}.
     *
     * @param enabled {@literal false} to disable this handler.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void initializeDefaultFields() {
        gelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(Time, Severity, ThreadName, SourceClassName,
                SourceMethodName, SourceSimpleClassName, LoggerName, NDC));
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return enabled && super.isLoggable(record);
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(ExtLogRecord.wrap(record));
    }

    @Override
    protected GelfMessageAssembler createGelfMessageAssembler() {
        return new MdcGelfMessageAssembler();
    }

    @Override
    protected GelfMessage createGelfMessage(final LogRecord record) {
        return getGelfMessageAssembler().createGelfMessage(new JBoss7JulLogEvent((ExtLogRecord) record));
    }

    public boolean isMdcProfiling() {
        return getGelfMessageAssembler().isMdcProfiling();
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        getGelfMessageAssembler().setMdcProfiling(mdcProfiling);
    }

    public boolean isIncludeFullMdc() {
        return getGelfMessageAssembler().isIncludeFullMdc();
    }

    public void setIncludeFullMdc(boolean includeFullMdc) {
        getGelfMessageAssembler().setIncludeFullMdc(includeFullMdc);
    }

    private MdcGelfMessageAssembler getGelfMessageAssembler() {
        return (MdcGelfMessageAssembler) gelfMessageAssembler;
    }
}
