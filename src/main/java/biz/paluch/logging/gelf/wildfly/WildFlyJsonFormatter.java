package biz.paluch.logging.gelf.wildfly;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.LoggerName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.NDC;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.Server;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.Severity;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceClassName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceMethodName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.SourceSimpleClassName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.ThreadName;
import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.Time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.logmanager.ExtFormatter;
import org.jboss.logmanager.ExtLogRecord;

import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.ConfigurationSupport;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.jboss7.JBoss7JulLogEvent;

/**
 * Log-Formatter for JSON using fields specified within GELF. This formatter will produce a JSON object for each log event.
 * Example:
 *
 * <code>
 {
    "NDC": "ndc message",
    "timestamp": "1439319236.722",
    "SourceClassName": "biz.paluch.logging.gelf.wildfly.WildFlyGelfLogFormatterTest",
    "SourceMethodName": "testDefaults",
    "level": "6",
    "SourceSimpleClassName": "WildFlyGelfLogFormatterTest",
    "facility": "logstash-gelf",
    "full_message": "foo bar test log message",
    "short_message": "foo bar test log message",
    "MySeverity": "INFO",
    "LoggerName": "biz.paluch.logging.gelf.wildfly.WildFlyGelfLogFormatterTest",
    "Thread": "main",
    "MyTime": "2015-08-11 20:53:56,0722"
}
 * </code>
 *
 * Following parameters are supported/needed:
 * <ul>
 * <li>lineBreak (Optional): End of line, defaults to {@code \n}</li>
 * <li>fields (Optional): Comma-separated list of log event fields that should be included in the JSON. Defaults to
 * {@code Time, Severity, ThreadName, SourceClassName, SourceMethodName, SourceSimpleClassName, LoggerName, NDC}</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field (true/false/throwable reference [0 = throwable, 1 = throwable.cause, -1 = root cause]), default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>includeLogMessageParameters (Optional): Include message parameters from the log event (see
 * {@link LogRecord#getParameters()}, default true</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg. fieldName=Value,field2=value2</li>
 * <li>additionalFieldTypes (Optional): Type specification for additional and MDC fields. Supported types: String, long, Long,
 * double, Double and discover (default if not specified, discover field type on parseability). Eg. field=String,field2=double</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * Application,Version,SomeOtherFieldName</li>
 * <li>dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular
 * expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name. mdc.*,[mdc|MDC]fields</li>
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
 * Two values are set by the formatter:
 * </p>
 * <ul>
 * <li>profiling.requestEnd: End-Time of the Request-End in Date.toString-representation</li>
 * <li>profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)</li>
 * </ul>
 *
 * @author Mark Paluch
 */
public class WildFlyJsonFormatter extends ExtFormatter {

    public static final String MULTI_VALUE_DELIMITTER = ",";
    private MdcGelfMessageAssembler gelfMessageAssembler = new MdcGelfMessageAssembler();
    private String lineBreak = "\n";
    private boolean wasSetFieldsCalled = false;

    public static final Set<LogMessageField.NamedLogField> SUPPORTED_FIELDS;

    static {
        Set<LogMessageField.NamedLogField> supportedFields = new LinkedHashSet<LogMessageField.NamedLogField>();

        supportedFields.add(Time);
        supportedFields.add(Severity);
        supportedFields.add(ThreadName);
        supportedFields.add(SourceClassName);
        supportedFields.add(SourceMethodName);
        supportedFields.add(SourceSimpleClassName);
        supportedFields.add(LoggerName);
        supportedFields.add(NDC);
        supportedFields.add(Server);

        SUPPORTED_FIELDS = Collections.unmodifiableSet(supportedFields);
    }

    /**
     * Create a new instance of the {@link WildFlyJsonFormatter}.
     */
    public WildFlyJsonFormatter() {

    }

    @Override
    public String format(ExtLogRecord extLogRecord) {
        if (!wasSetFieldsCalled) {
            addFields(SUPPORTED_FIELDS);
        }

        GelfMessage gelfMessage = gelfMessageAssembler.createGelfMessage(new JBoss7JulLogEvent(extLogRecord));
        return gelfMessage.toJson("") + getLineBreak();
    }

    public void setFields(String fieldSpec) {

        String[] properties = fieldSpec.split(MULTI_VALUE_DELIMITTER);
        List<LogMessageField.NamedLogField> fields = new ArrayList<LogMessageField.NamedLogField>();
        for (String field : properties) {

            LogMessageField.NamedLogField namedLogField = LogMessageField.NamedLogField.byName(field.trim());
            if (namedLogField == null) {
                throw new IllegalArgumentException("Cannot resolve field name '" + field
                        + "' to a field. Supported field names are: " + SUPPORTED_FIELDS);
            }

            if (!SUPPORTED_FIELDS.contains(namedLogField)) {
                throw new IllegalArgumentException("Field '" + field + "' is not supported. Supported field names are: "
                        + SUPPORTED_FIELDS);
            }

            fields.add(namedLogField);
        }

        addFields(fields);

    }

    private void addFields(Collection<LogMessageField.NamedLogField> fields) {
        gelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(fields
                .toArray(new LogMessageField.NamedLogField[fields.size()])));

        wasSetFieldsCalled = true;
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

    public boolean isMdcProfiling() {
        return gelfMessageAssembler.isMdcProfiling();
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        gelfMessageAssembler.setMdcProfiling(mdcProfiling);
    }

    public boolean isIncludeFullMdc() {
        return gelfMessageAssembler.isIncludeFullMdc();
    }

    public void setIncludeFullMdc(boolean includeFullMdc) {
        gelfMessageAssembler.setIncludeFullMdc(includeFullMdc);
    }

    public String getOriginHost() {
        return gelfMessageAssembler.getOriginHost();
    }

    public void setOriginHost(String originHost) {
        gelfMessageAssembler.setOriginHost(originHost);
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

    public boolean isFilterStackTrace() {
        return gelfMessageAssembler.isFilterStackTrace();
    }

    public void setFilterStackTrace(boolean filterStackTrace) {
        gelfMessageAssembler.setFilterStackTrace(filterStackTrace);
    }

    public String getTimestampPattern() {
        return gelfMessageAssembler.getTimestampPattern();
    }

    public void setTimestampPattern(String timestampPattern) {
        gelfMessageAssembler.setTimestampPattern(timestampPattern);
    }

    public String getVersion() {
        return gelfMessageAssembler.getVersion();
    }

    public void setVersion(String version) {
        gelfMessageAssembler.setVersion(version);
    }

    public String getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(String lineBreak) {
        this.lineBreak = lineBreak;
    }

    public boolean isIncludeLogMessageParameters() {
        return gelfMessageAssembler.isIncludeLogMessageParameters();
    }

    public void setIncludeLogMessageParameters(boolean includeLogMessageParameters) {
        gelfMessageAssembler.setIncludeLogMessageParameters(includeLogMessageParameters);
    }
}
