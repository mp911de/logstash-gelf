package biz.paluch.logging.gelf.jboss7;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.StaticMessageField;
import biz.paluch.logging.gelf.intern.GelfMessage;

import java.util.logging.LogRecord;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Java-Util-Logging Handler creates GELF Messages and posts
 * them using UDP (default) or TCP. Following parameters are supported/needed:
 * <p/>
 * <ul>
 * <li>graylogHost (Mandatory): Hostname/IP-Address of the Logstash Host
 * <ul>
 * <li>tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com</li>
 * <li>udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com</li>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * </ul>
 * </li>
 * <li>port (Optional): Port, default 12201</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStacktrace (Optional): Post Stack-Trace to StackTrace field, default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>level (Optional): Log-Level, default INFO</li>
 * <li>filter (Optional): Class-Name of a Log-Filter, default none</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg.
 * .GelfLogHandler.additionalFields=fieldName=Value,field2=value2</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * .JBoss7GelfLogHandler.mdcFields=Application,Version,SomeOtherFieldName</li>
 * <li>dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular
 * expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name.
 * .JBoss7GelfLogHandler.dynamicMdcFields=mdc.*,[mdc|MDC]fields</li>
 * <li>includeFullMdc (Optional): Include all fields from the MDC, default false</li>
 * </ul>
 * <p/>
 * <a name="mdcProfiling"></a>
 * <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must
 * set one value in the MDC:
 * <ul>
 * <li>profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)</li>
 * </ul>
 * <p/>
 * Two values are set by the Log Appender:
 * <ul>
 * <li>profiling.requestEnd: End-Time of the Request-End in Date.toString-representation</li>
 * <li>profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)</li>
 * </ul>
 * <p/>
 * </p>
 */
public class JBoss7GelfLogHandler extends biz.paluch.logging.gelf.jul.GelfLogHandler {

    public JBoss7GelfLogHandler() {
    }

    @Override
    protected GelfMessageAssembler createGelfMessageAssembler() {
        return new MdcGelfMessageAssembler();
    }

    protected GelfMessage createGelfMessage(final LogRecord record) {
        return getGelfMessageAssembler().createGelfMessage(new JBoss7JulLogEvent(record));
    }

    public void setAdditionalFields(String fieldSpec) {

        String[] properties = fieldSpec.split(",");

        for (String field : properties) {
            final int index = field.indexOf('=');
            if (-1 != index) {
                getGelfMessageAssembler().addField(
                        new StaticMessageField(field.substring(0, index), field.substring(index + 1)));
            }
        }
    }

    public void setMdcFields(String fieldSpec) {
        String[] fields = fieldSpec.split(",");

        for (String field : fields) {
            getGelfMessageAssembler().addField(new MdcMessageField(field.trim(), field.trim()));
        }
    }

    public void setDynamicMdcFields(String fieldSpec) {
        String[] fields = fieldSpec.split(",");

        for (String field : fields) {
            gelfMessageAssembler.addField(new DynamicMdcMessageField(field.trim()));
        }
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
