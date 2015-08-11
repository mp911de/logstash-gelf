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

import org.jboss.logmanager.ExtFormatter;
import org.jboss.logmanager.ExtLogRecord;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.StaticMessageField;
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
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStacktrace (Optional): Post Stack-Trace to StackTrace field, default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg. fieldName=Value,field2=value2</li>
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
 */
public class WildFlyJsonFormatter extends ExtFormatter {

    private MdcGelfMessageAssembler gelfMessageAssembler = new MdcGelfMessageAssembler();
    private String lineBreak = "\n";

    /**
     * Create a new instance of the {@link WildFlyJsonFormatter}.
     */
    public WildFlyJsonFormatter() {
        gelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(Time, Severity, ThreadName, SourceClassName,
                SourceMethodName, SourceSimpleClassName, LoggerName, NDC, Server));
    }

    @Override
    public String format(ExtLogRecord extLogRecord) {
        GelfMessage gelfMessage = gelfMessageAssembler.createGelfMessage(new JBoss7JulLogEvent(extLogRecord));
        return gelfMessage.toJson("") + lineBreak;
    }

    public void setAdditionalFields(String fieldSpec) {

        String[] properties = fieldSpec.split(",");

        for (String field : properties) {
            final int index = field.indexOf('=');
            if (-1 != index) {
                gelfMessageAssembler.addField(new StaticMessageField(field.substring(0, index), field.substring(index + 1)));
            }
        }
    }

    public void setMdcFields(String fieldSpec) {
        String[] fields = fieldSpec.split(",");

        for (String field : fields) {
            gelfMessageAssembler.addField(new MdcMessageField(field.trim(), field.trim()));
        }
    }

    public void setDynamicMdcFields(String fieldSpec) {
        String[] fields = fieldSpec.split(",");

        for (String field : fields) {
            gelfMessageAssembler.addField(new DynamicMdcMessageField(field.trim()));
        }
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

    public boolean isExtractStackTrace() {
        return gelfMessageAssembler.isExtractStackTrace();
    }

    public void setExtractStackTrace(boolean extractStacktrace) {
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
}
