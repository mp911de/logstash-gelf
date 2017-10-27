package biz.paluch.logging.gelf.logback;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.LoggerName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.Marker;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.Severity;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceClassName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceLineNumber;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceMethodName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceSimpleClassName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.ThreadName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.Time;

import java.util.Collections;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ConfigurationSupport;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Logback Handler creates GELF Messages and posts them using
 * UDP (default) or TCP. Following parameters are supported/needed:
 * <ul>
 * <li>host (Mandatory): Hostname/IP-Address of the Logstash Host
 * <ul>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * <li>See docs for more details</li>
 * </ul>
 * </li>
 * <li>port (Optional): Port, default 12201</li>
 * <li>version (Optional): GELF Version 1.0 or 1.1, default 1.0</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field (true/false/throwable reference [0 = throwable, 1 = throwable.cause, -1 = root cause]), default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>filter (Optional): logback filter (incl. log level)</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg.
 * .GelfLogHandler.additionalFields=fieldName=Value,field2=value2</li>
 * <li>additionalFieldTypes (Optional): Type specification for additional and MDC fields. Supported types: String, long, Long,
 * double, Double and discover (default if not specified, discover field type on parseability). Eg. field=String,field2=double</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * mdcFields=Application,Version,SomeOtherFieldName</li>
 * <li>dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular
 * expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name.</li>
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
 * The {@link #append(ILoggingEvent)} method is thread-safe and may be called by different threads at any time.
 *
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
public class GelfLogbackAppender extends AppenderBase<ILoggingEvent> implements ErrorReporter {

    protected GelfSender gelfSender;
    protected MdcGelfMessageAssembler gelfMessageAssembler;

    public GelfLogbackAppender() {
        super();
        gelfMessageAssembler = new MdcGelfMessageAssembler();
        gelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(Time, Severity, ThreadName, SourceClassName,
                SourceMethodName, SourceLineNumber, SourceSimpleClassName, LoggerName, Marker));
    }

    @Override
    protected void append(ILoggingEvent event) {

        if (event == null) {
            return;
        }

        try {
            GelfMessage message = createGelfMessage(event);
            if (!message.isValid()) {
                reportError("GELF Message is invalid: " + message.toJson(), null);
                return;
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null);
            }
        } catch (Exception e) {
            reportError("Could not send GELF message: " + e.getMessage(), e);
        }
    }

    @Override
    public void start() {

        if (null == gelfSender) {
            RuntimeContainer.initialize(this);
            gelfSender = createGelfSender();
        }

        super.start();
    }

    @Override
    public void stop() {

        if (null != gelfSender) {
            Closer.close(gelfSender);
            gelfSender = null;
        }

        super.stop();
    }

    protected GelfSender createGelfSender() {
        return GelfSenderFactory.createSender(gelfMessageAssembler, this, Collections.EMPTY_MAP);
    }

    public void reportError(String message, Exception exception) {
        addError(message, exception);
    }

    protected GelfMessage createGelfMessage(final ILoggingEvent loggingEvent) {
        return gelfMessageAssembler.createGelfMessage(new LogbackLogEvent(loggingEvent));
    }

    public void setAdditionalFields(String spec) {
        ConfigurationSupport.setAdditionalFields(spec, gelfMessageAssembler);
    }

    public void setAdditionalFieldTypes(String spec) {
        ConfigurationSupport.setAdditionalFieldTypes(spec, gelfMessageAssembler);
    }

    public void setMdcFields(String spec) {
        ConfigurationSupport.setMdcFields(spec, gelfMessageAssembler);
    }

    public void setDynamicMdcFields(String spec) {
        ConfigurationSupport.setDynamicMdcFields(spec, gelfMessageAssembler);
    }

    public String getGraylogHost() {
        return gelfMessageAssembler.getHost();
    }

    public void setGraylogHost(String graylogHost) {
        gelfMessageAssembler.setHost(graylogHost);
    }

    public String getOriginHost() {
        return gelfMessageAssembler.getOriginHost();
    }

    public void setOriginHost(String originHost) {
        gelfMessageAssembler.setOriginHost(originHost);
    }

    public int getGraylogPort() {
        return gelfMessageAssembler.getPort();
    }

    public void setGraylogPort(int graylogPort) {
        gelfMessageAssembler.setPort(graylogPort);
    }

    public String getHost() {
        return gelfMessageAssembler.getHost();
    }

    public void setHost(String host) {
        gelfMessageAssembler.setHost(host);
    }

    public int getPort() {
        return gelfMessageAssembler.getPort();
    }

    public void setPort(int port) {
        gelfMessageAssembler.setPort(port);
    }

    public String getFacility() {
        return gelfMessageAssembler.getFacility();
    }

    public void setFacility(String facility) {
        gelfMessageAssembler.setFacility(facility);
    }

    public String getExtractStackTrace() {
        return gelfMessageAssembler.getExtractStackTrace();
    }

    public void setExtractStackTrace(String extractStacktrace) {
        gelfMessageAssembler.setExtractStackTrace(extractStacktrace);
    }

    public String getAppendStackTraceToFullMessage() {
        return gelfMessageAssembler.getAppendStackTraceToFullMessage();
    }

    public void setAppendStackTraceToFullMessage(String appendStackTraceToFullMessage) {
        gelfMessageAssembler.setAppendStackTraceToFullMessage(appendStackTraceToFullMessage);
    }

    public boolean isFilterStackTrace() {
        return gelfMessageAssembler.isFilterStackTrace();
    }

    public void setFilterStackTrace(boolean filterStackTrace) {
        gelfMessageAssembler.setFilterStackTrace(filterStackTrace);
    }

    public boolean isMdcProfiling() {
        return gelfMessageAssembler.isMdcProfiling();
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        gelfMessageAssembler.setMdcProfiling(mdcProfiling);
    }

    public String getTimestampPattern() {
        return gelfMessageAssembler.getTimestampPattern();
    }

    public void setTimestampPattern(String timestampPattern) {
        gelfMessageAssembler.setTimestampPattern(timestampPattern);
    }

    public int getMaximumMessageSize() {
        return gelfMessageAssembler.getMaximumMessageSize();
    }

    public void setMaximumMessageSize(int maximumMessageSize) {
        gelfMessageAssembler.setMaximumMessageSize(maximumMessageSize);
    }

    public boolean isIncludeFullMdc() {
        return gelfMessageAssembler.isIncludeFullMdc();
    }

    public void setIncludeFullMdc(boolean includeFullMdc) {
        gelfMessageAssembler.setIncludeFullMdc(includeFullMdc);
    }

    public String getVersion() {
        return gelfMessageAssembler.getVersion();
    }

    public void setVersion(String version) {
        gelfMessageAssembler.setVersion(version);
    }

}
