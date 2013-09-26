package biz.paluch.logging.gelf.jul;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Java-Util-Logging Handler creates GELF Messages and posts
 * them using UDP (default) or TCP. Following parameters are supported/needed:
 * <p/>
 * <ul>
 * <li>graylogHost (Mandatory): Hostname/IP-Address of the Graylog Host
 * <ul>
 * <li>tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com</li>
 * <li>udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com</li>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * </ul>
 * </li>
 * <li>graylogPort (Optional): Port, default 12201</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStacktrace (Optional): Post Stack-Trace to StackTrace field, default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>level (Optional): Log-Level, default INFO</li>
 * <li>filter (Optional): Class-Name of a Log-Filter, default none</li>
 * <li>additionalField.(number) (Optional): Post additional fields. Eg. .GelfLogHandler.additionalField.0=fieldName=Value</li>
 * <li>mdcField.(number) (Optional): Post additional fields, pull Values from MDC Eg. .GelfLogHandler.mdcField.0=Application</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * .GelfLogHandler.mdcFields=Application,Version,SomeOtherFieldName</li>
 * </ul>
 * <p/>
 * <a name="mdcProfiling"></a>
 * <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must
 * set one valuev in the MDC:
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
public class GelfLogHandler extends Handler {

    private GelfSender gelfSender;
    private GelfMessageAssembler gelfMessageAssembler;

    public GelfLogHandler() {
        gelfMessageAssembler = new GelfMessageAssembler();

        JulFrameworkPropertyProvider propertyProvider = new JulFrameworkPropertyProvider(GelfLogHandler.class);

        final String level = propertyProvider.getProperty("level");
        if (null != level) {
            setLevel(Level.parse(level.trim()));
        } else {
            setLevel(Level.INFO);
        }

        final String filter = propertyProvider.getProperty("filter");
        try {
            if (null != filter) {
                final Class clazz = ClassLoader.getSystemClassLoader().loadClass(filter);
                setFilter((Filter) clazz.newInstance());
            }
        } catch (final Exception e) {
            // ignore
        }
        // This only used for testing
        final String testSender = propertyProvider.getProperty("graylogTestSenderClass");
        try {
            if (null != testSender) {
                final Class clazz = ClassLoader.getSystemClassLoader().loadClass(testSender);
                gelfSender = (GelfSender) clazz.newInstance();
            }
        } catch (final Exception e) {
            // ignore
        }

    }

    @Override
    public synchronized void flush() {
    }

    @Override
    public synchronized void publish(final LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        if (null == gelfSender) {
            if (gelfMessageAssembler.getGraylogHost() == null) {
                reportError("Graylog2 hostname is empty!", null, 1);
            } else {
                try {
                    this.gelfSender = GelfSenderFactory
                            .createSender(gelfMessageAssembler.getGraylogHost(), gelfMessageAssembler.getGraylogPort());
                } catch (UnknownHostException e) {
                    reportError("Unknown Graylog2 hostname:" + gelfMessageAssembler.getGraylogHost(), e,
                            ErrorManager.WRITE_FAILURE);
                } catch (SocketException e) {
                    reportError("Socket exception", e, ErrorManager.WRITE_FAILURE);
                } catch (IOException e) {
                    reportError("IO exception", e, ErrorManager.WRITE_FAILURE);
                }
            }
        }

        try {
            GelfMessage message = createGelfMessage(record);
            if (!message.isValid()) {
                reportError("GELF Message is invalid: " + message.toJson(), null, ErrorManager.WRITE_FAILURE);
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null, ErrorManager.WRITE_FAILURE);
            }
        } catch (Exception e) {
            reportError("Could not send GELF message", e, ErrorManager.FORMAT_FAILURE);
        }
    }

    @Override
    public void close() {
        if (null != gelfSender) {
            gelfSender.close();
            gelfSender = null;
        }
    }

    private GelfMessage createGelfMessage(final LogRecord record) {
        return gelfMessageAssembler.createGelfMessage(new JulLogRecordEventProvider(record));
    }

    public void setAdditionalFields(String fieldSpec) {

        String[] properties = fieldSpec.split(",");

        for (String field : properties) {
            final int index = field.indexOf('=');
            if (-1 != index) {
                gelfMessageAssembler.getFields().put(field.substring(0, index), field.substring(index + 1));
            }
        }
    }

    public void setMdcFields(String fieldSpec) {
        String[] fields = fieldSpec.split(",");

        Set<String> mdcFields = new HashSet<String>();
        for (String field : fields) {
            mdcFields.add(field.trim());
        }
        gelfMessageAssembler.setMdcFields(mdcFields);
    }

    public String getGraylogHost() {
        return gelfMessageAssembler.getGraylogHost();
    }

    public void setGraylogHost(String graylogHost) {
        gelfMessageAssembler.setGraylogHost(graylogHost);
    }

    public String getOriginHost() {
        return gelfMessageAssembler.getOriginHost();
    }

    public void setOriginHost(String originHost) {
        gelfMessageAssembler.setOriginHost(originHost);
    }

    public int getGraylogPort() {
        return gelfMessageAssembler.getGraylogPort();
    }

    public void setGraylogPort(int graylogPort) {
        gelfMessageAssembler.setGraylogPort(graylogPort);
    }

    public String getFacility() {
        return gelfMessageAssembler.getFacility();
    }

    public void setFacility(String facility) {
        gelfMessageAssembler.setFacility(facility);
    }

    public boolean isExtractStacktrace() {
        return gelfMessageAssembler.isExtractStacktrace();
    }

    public void setExtractStacktrace(boolean extractStacktrace) {
        gelfMessageAssembler.setExtractStacktrace(extractStacktrace);
    }

    public boolean isFilterStackTrace() {
        return gelfMessageAssembler.isFilterStackTrace();
    }

    public void setFilterStackTrace(boolean filterStackTrace) {
        gelfMessageAssembler.setFilterStackTrace(filterStackTrace);
    }

    public Map<String, String> getFields() {
        return gelfMessageAssembler.getFields();
    }

    public void setFields(Map<String, String> fields) {
        gelfMessageAssembler.setFields(fields);
    }

    public Set<String> getMdcFields() {
        return gelfMessageAssembler.getMdcFields();
    }

    public void setMdcFields(Set<String> mdcFields) {
        gelfMessageAssembler.setMdcFields(mdcFields);
    }

    public boolean isMdcProfiling() {
        return gelfMessageAssembler.isMdcProfiling();
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        gelfMessageAssembler.setMdcProfiling(mdcProfiling);
    }
}
