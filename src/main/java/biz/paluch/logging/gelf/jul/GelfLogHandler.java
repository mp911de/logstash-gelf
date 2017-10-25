package biz.paluch.logging.gelf.jul;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.*;

import java.util.Collections;
import java.util.logging.*;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.PropertyProvider;
import biz.paluch.logging.gelf.intern.*;

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
 * <li>port (Optional): Port, default 12201</li>
 * <li>version (Optional): GELF Version 1.0 or 1.1, default 1.0</li>
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
 * <li>additionalField.(number) (Optional): Post additional fields. Eg. .GelfLogHandler.additionalField.0=fieldName=Value</li>
 * </ul>
 *
 * The {@link #publish(LogRecord)} method is thread-safe and may be called by different threads at any time.
 *
 * @author Mark Paluch
 */
public class GelfLogHandler extends Handler implements ErrorReporter {

    protected volatile GelfSender gelfSender;
    protected GelfMessageAssembler gelfMessageAssembler;

    public GelfLogHandler() {
        super();

        RuntimeContainer.initialize(this);
        gelfMessageAssembler = createGelfMessageAssembler();

        initializeDefaultFields();

        JulPropertyProvider propertyProvider = new JulPropertyProvider(GelfLogHandler.class);
        gelfMessageAssembler.initialize(propertyProvider);

        String level = propertyProvider.getProperty(PropertyProvider.PROPERTY_LEVEL);
        if (null != level) {
            setLevel(Level.parse(level.trim()));
        } else {
            setLevel(Level.INFO);
        }

        String additionalFields = propertyProvider.getProperty(PropertyProvider.PROPERTY_ADDITIONAL_FIELDS);
        if (null != additionalFields) {
            setAdditionalFields(additionalFields);
        }

        String additionalFieldTypes = propertyProvider.getProperty(PropertyProvider.PROPERTY_ADDITIONAL_FIELD_TYPES);
        if (null != additionalFieldTypes) {
            setAdditionalFieldTypes(additionalFieldTypes);
        }

        String filter = propertyProvider.getProperty(PropertyProvider.PROPERTY_FILTER);
        try {
            if (null != filter) {
                final Class clazz = ClassLoader.getSystemClassLoader().loadClass(filter);
                setFilter((Filter) clazz.newInstance());
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    protected void initializeDefaultFields() {
        gelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(Time, Severity, ThreadName, SourceClassName,
                SourceMethodName, SourceSimpleClassName, LoggerName));
    }

    protected GelfMessageAssembler createGelfMessageAssembler() {
        return new GelfMessageAssembler();
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(final LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        try {
            if (null == gelfSender) {
                synchronized (this) {
                    if (null == gelfSender) {
                        gelfSender = createGelfSender();
                    }
                }
            }
        } catch (Exception e) {
            reportError("Could not send GELF message: " + e.getMessage(), e, ErrorManager.OPEN_FAILURE);
            return;
        }

        try {
            GelfMessage message = createGelfMessage(record);
            if (!message.isValid()) {
                reportError("GELF Message is invalid: " + message.toJson(), null, ErrorManager.WRITE_FAILURE);
                return;
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null, ErrorManager.WRITE_FAILURE);
            }
        } catch (Exception e) {
            reportError("Could not send GELF message: " + e.getMessage(), e, ErrorManager.FORMAT_FAILURE);
        }
    }

    protected GelfSender createGelfSender() {
        return GelfSenderFactory.createSender(gelfMessageAssembler, this, Collections.EMPTY_MAP);
    }

    @Override
    public void reportError(String message, Exception e) {
        reportError(message, e, ErrorManager.GENERIC_FAILURE);
    }

    @Override
    public void close() {
        if (null != gelfSender) {
            Closer.close(gelfSender);
            gelfSender = null;
        }
    }

    protected GelfMessage createGelfMessage(final LogRecord record) {
        return gelfMessageAssembler.createGelfMessage(new JulLogEvent(record));
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

    public int getGraylogPort() {
        return gelfMessageAssembler.getPort();
    }

    public void setGraylogPort(int graylogPort) {
        gelfMessageAssembler.setPort(graylogPort);
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

    public boolean isIncludeLogMessageParameters() {
        return gelfMessageAssembler.isIncludeLogMessageParameters();
    }

    public void setIncludeLogMessageParameters(boolean includeLogMessageParameters) {
        gelfMessageAssembler.setIncludeLogMessageParameters(includeLogMessageParameters);
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

    public String getVersion() {
        return gelfMessageAssembler.getVersion();
    }

    public void setVersion(String version) {
        gelfMessageAssembler.setVersion(version);
    }
}
