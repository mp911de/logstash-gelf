package biz.paluch.logging.gelf.logback;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Logback Handler creates GELF Messages and posts them
 * using UDP (default) or TCP. Following parameters are supported/needed:
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
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>filter (Optional): logback filter (incl. log level)</li>
 * <li>additionalFields(number) (Optional): Post additional fields. Eg.
 * .GelfLogHandler.additionalFields=fieldName=Value,field2=value2</li>
 * <li>mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated
 * mdcFields=Application,Version,SomeOtherFieldName</li>
 * </ul>
 * <p/>
 * <a name="mdcProfiling"></a>
 * <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated.
 * You must set one value in the MDC:
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
 *
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
public class GelfLogbackAppender extends AppenderBase<ILoggingEvent> {

    protected GelfSender gelfSender;
    protected MdcLogbackGelfMessageAssembler gelfMessageAssembler;

    public GelfLogbackAppender() {
        gelfMessageAssembler = new MdcLogbackGelfMessageAssembler();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event == null) {
            return;
        }

        if (null == gelfSender) {
            if (gelfMessageAssembler.getGraylogHost() == null) {
                reportError("Graylog2 hostname is empty!", null);
            } else {
                try {
                    this.gelfSender = GelfSenderFactory.createSender(gelfMessageAssembler.getGraylogHost(),
                            gelfMessageAssembler.getGraylogPort());
                } catch (UnknownHostException e) {
                    reportError("Unknown Graylog2 hostname:" + gelfMessageAssembler.getGraylogHost(), e);
                } catch (SocketException e) {
                    reportError("Socket exception", e);
                } catch (IOException e) {
                    reportError("IO exception", e);
                }
            }
        }

        try {
            GelfMessage message = createGelfMessage(event);
            if (!message.isValid()) {
                reportError("GELF Message is invalid: " + message.toJson(), null);
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null);
            }
        } catch (Exception e) {
            reportError("Could not send GELF message", e);
        }
    }

    private void reportError(String message, Exception exception) {
        addError(message, exception);
    }

    protected GelfMessage createGelfMessage(final ILoggingEvent loggingEvent) {
        return gelfMessageAssembler.createGelfMessage(new LogbackLogEvent(loggingEvent));
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

    public void setTestSenderClass(String testSender) {
        // This only used for testing
        try {
            if (null != testSender) {
                final Class clazz = Class.forName(testSender);
                gelfSender = (GelfSender) clazz.newInstance();
            }
        } catch (final Exception e) {
            reportError("Could not instantiate the testSenderClass", e);
        }
    }
}
