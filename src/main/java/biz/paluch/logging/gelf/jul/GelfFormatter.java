package biz.paluch.logging.gelf.jul;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.*;

import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.ConfigurationSupport;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Log-Formatter for JSON using fields specified within GELF. This formatter will produce a JSON object for each log event.
 * Example:
 *
 * <code>
 {
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
    "Time": "2015-08-11 20:53:56,0722"
 }
 * </code>
 *
 * Following parameters are supported/needed:
 * <ul>
 * <li>lineBreak (Optional): End of line, platform dependent default value, see {@code System.getProperty("line.separator")}</li>
 * <li>fields (Optional): Comma-separated list of log event fields that should be included in the JSON. Defaults to
 * {@code Time, Severity, ThreadName, SourceClassName, SourceMethodName, SourceSimpleClassName, LoggerName}</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field (true/false/throwable reference [0 = throwable, 1 =
 * throwable.cause, -1 = root cause]), default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>includeLocation (Optional): Include source code location, default true</li>
 * <li>includeLogMessageParameters (Optional): Include message parameters from the log event (see
 * {@link LogRecord#getParameters()}, default true</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg. fieldName=Value,field2=value2</li>
 * <li>additionalFieldTypes (Optional): Type specification for additional and MDC fields. Supported types: String, long, Long,
 * double, Double and discover (default if not specified, discover field type on parseability). Eg. field=String,field2=double</li>
 * </ul>
 *
 * @author Greg Peterson
 * @since 1.14.0
 */
public class GelfFormatter extends Formatter {

    public static final String MULTI_VALUE_DELIMITTER = ",";
    public static final Set<LogMessageField.NamedLogField> SUPPORTED_FIELDS;

    private final MdcGelfMessageAssembler gelfMessageAssembler = new MdcGelfMessageAssembler();
    private String lineBreak = System.getProperty("line.separator");
    private boolean wasSetFieldsCalled = false;

    static {
        Set<LogMessageField.NamedLogField> supportedFields = new LinkedHashSet<LogMessageField.NamedLogField>();

        supportedFields.add(Time);
        supportedFields.add(Severity);
        supportedFields.add(ThreadName);
        supportedFields.add(SourceClassName);
        supportedFields.add(SourceMethodName);
        supportedFields.add(SourceSimpleClassName);
        supportedFields.add(LoggerName);
        supportedFields.add(Server);

        SUPPORTED_FIELDS = Collections.unmodifiableSet(supportedFields);
    }

    public GelfFormatter() {
        super();
        configure();
    }

    private void configure() {
        String cname = getClass().getName();
        LogManager manager = LogManager.getLogManager();

        String val = manager.getProperty(cname + ".fields");
        if (val != null) {
            setFields(val);
        }

        val = manager.getProperty(cname + ".version");
        if (val != null) {
            setVersion(val);
        }

        val = manager.getProperty(cname + ".facility");
        if (val != null) {
            setFacility(val);
        }

        val = manager.getProperty(cname + ".extractStackTrace");
        if (val != null) {
            setExtractStackTrace(val);
        }

        val = manager.getProperty(cname + ".filterStackTrace");
        if (val != null) {
            setFilterStackTrace(Boolean.valueOf(val));
        }

        val = manager.getProperty(cname + ".includeLogMessageParameters");
        if (val != null) {
            setIncludeLogMessageParameters(Boolean.valueOf(val));
        }

        val = manager.getProperty(cname + ".includeLocation");
        if (val != null) {
            setIncludeLocation(Boolean.valueOf(val));
        }

        val = manager.getProperty(cname + ".timestampPattern");
        if (val != null) {
            setTimestampPattern(val);
        }

        val = manager.getProperty(cname + ".additionalFields");
        if (val != null) {
            setAdditionalFields(val);
        }

        val = manager.getProperty(cname + ".additionalFieldTypes");
        if (val != null) {
            setAdditionalFieldTypes(val);
        }

        val = manager.getProperty(cname + ".originHost");
        if (val != null) {
            setOriginHost(val);
        }

        val = manager.getProperty(cname + ".linebreak");
        if (val != null) {
            setLineBreak(val);
        }
    }

    @Override
    public String format(final LogRecord record) {
        if (!wasSetFieldsCalled) {
            addFields(SUPPORTED_FIELDS);
        }
        GelfMessage gelfMessage = gelfMessageAssembler.createGelfMessage(new JulLogEvent(record));
        return gelfMessage.toJson("") + lineBreak;
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

    public boolean isIncludeLogMessageParameters() {
        return gelfMessageAssembler.isIncludeLogMessageParameters();
    }

    public void setIncludeLogMessageParameters(boolean includeLogMessageParameters) {
        gelfMessageAssembler.setIncludeLogMessageParameters(includeLogMessageParameters);
    }

    public boolean isIncludeLocation() {
        return gelfMessageAssembler.isIncludeLocation();
    }

    public void setIncludeLocation(boolean includeLocation) {
        gelfMessageAssembler.setIncludeLocation(includeLocation);
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
}
