package biz.paluch.logging.gelf;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.StackTraceFilter;
import biz.paluch.logging.gelf.intern.GelfMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:05
 */
public class GelfMessageAssembler {

    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;

    public static final String PROPERTY_GRAYLOG_HOST = "graylogHost";
    public static final String PROPERTY_GRAYLOG_PORT = "graylogPort";
    public static final String PROPERTY_ORIGIN_HOST = "originHost";
    public static final String PROPERTY_EXTRACT_STACKTRACE = "extractStackTrace";
    public static final String PROPERTY_FILTER_STACK_TRACE = "filterStackTrace";
    public static final String PROPERTY_FACILITY = "facility";
    public static final String PROPERTY_MAX_MESSAGE_SIZE = "maximumMessageSize";
    public static final String PROPERTY_ADDITIONAL_FIELD = "additionalField.";

    public static final String FIELD_TIME = "Time";
    public static final String FIELD_SEVERITY = "Severity";
    public static final String FIELD_THREAD = "Thread";
    public static final String FIELD_SOURCE_CLASS_NAME = "SourceClassName";
    public static final String FIELD_SOURCE_SIMPLE_CLASS_NAME = "SourceSimpleClassName";
    public static final String FIELD_SOURCE_METHOD_NAME = "SourceMethodName";
    public static final String FIELD_MESSAGE_PARAM = "MessageParam";
    public static final String FIELD_SERVER = "Server";
    public static final String FIELD_STACK_TRACE = "StackTrace";

    private String graylogHost;
    private String originHost;
    private int graylogPort;
    private String facility;
    private boolean extractStackTrace;
    private boolean filterStackTrace;
    private int maximumMessageSize;

    private Map<String, String> fields = new HashMap<String, String>();

    private String timestampPattern = "yyyy-MM-dd HH:mm:ss,SSSS";

    /**
     * Initialize datastructure from property provider.
     * 
     * @param propertyProvider
     */
    public void initialize(PropertyProvider propertyProvider) {
        graylogHost = propertyProvider.getProperty(PROPERTY_GRAYLOG_HOST);

        String port = propertyProvider.getProperty(PROPERTY_GRAYLOG_PORT);
        graylogPort = null == port ? 12201 : Integer.parseInt(port);

        originHost = propertyProvider.getProperty(PROPERTY_ORIGIN_HOST);
        extractStackTrace = "true".equalsIgnoreCase(propertyProvider.getProperty(PROPERTY_EXTRACT_STACKTRACE));
        filterStackTrace = "true".equalsIgnoreCase(propertyProvider.getProperty(PROPERTY_FILTER_STACK_TRACE));

        setupStaticFields(propertyProvider);
        facility = propertyProvider.getProperty(PROPERTY_FACILITY);

        String messageSize = propertyProvider.getProperty(PROPERTY_MAX_MESSAGE_SIZE);
        maximumMessageSize = null == port ? 8192 : Integer.parseInt(messageSize);
    }

    /**
     * Producte a Gelf message.
     * 
     * @param logEvent
     * @return GelfMessage
     */
    public GelfMessage createGelfMessage(LogEvent logEvent) {
        String message = logEvent.getMessage();

        String shortMessage = message;
        if (message.length() > MAX_SHORT_MESSAGE_LENGTH) {
            shortMessage = message.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        }

        final GelfMessage gelfMessage = new GelfMessage(shortMessage, message, logEvent.getLogTimestamp(),
                logEvent.getSyslogLevel());

        SimpleDateFormat dateFormat = new SimpleDateFormat(timestampPattern);
        gelfMessage.addField(FIELD_TIME, dateFormat.format(new Date(logEvent.getLogTimestamp())));
        gelfMessage.addField(FIELD_SEVERITY, logEvent.getLevelName());

        gelfMessage.addField(FIELD_THREAD, logEvent.getThreadName());
        gelfMessage.addField(FIELD_SOURCE_CLASS_NAME, logEvent.getSourceClassName());

        String simpleClassName = logEvent.getSourceClassName();
        int index = simpleClassName.lastIndexOf('.');
        if (index != -1) {
            simpleClassName = simpleClassName.substring(index + 1);
        }

        gelfMessage.addField(FIELD_SOURCE_SIMPLE_CLASS_NAME, simpleClassName);
        gelfMessage.addField(FIELD_SOURCE_METHOD_NAME, logEvent.getSourceMethodName());

        if (extractStackTrace) {
            addStackTrace(logEvent, gelfMessage);
        }

        if (logEvent.getParameters() != null) {
            for (int i = 0; i < logEvent.getParameters().length; i++) {
                Object param = logEvent.getParameters()[i];
                gelfMessage.addField(FIELD_MESSAGE_PARAM + i, "" + param);
            }
        }

        gelfMessage.addField(FIELD_SERVER, getOriginHost());
        gelfMessage.setHost(getOriginHost());

        if (null != facility) {
            gelfMessage.setFacility(facility);
        }

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            gelfMessage.addField(entry.getKey(), entry.getValue());
        }

        gelfMessage.setMaximumMessageSize(maximumMessageSize);
        return gelfMessage;
    }

    private void addStackTrace(LogEvent logEvent, GelfMessage gelfMessage) {
        final Throwable thrown = logEvent.getThrowable();
        if (null != thrown) {
            if (filterStackTrace) {
                gelfMessage.addField(FIELD_STACK_TRACE, StackTraceFilter.getFilteredStackTrace(thrown));
            } else {
                final StringWriter sw = new StringWriter();
                thrown.printStackTrace(new PrintWriter(sw));
                gelfMessage.addField(FIELD_STACK_TRACE, sw.toString());
            }
        }
    }

    private void setupStaticFields(PropertyProvider propertyProvider) {
        int fieldNumber = 0;
        fields = new HashMap<String, String>();
        while (true) {
            final String property = propertyProvider.getProperty(PROPERTY_ADDITIONAL_FIELD + fieldNumber);
            if (null == property) {
                break;
            }
            final int index = property.indexOf('=');
            if (-1 != index) {
                fields.put(property.substring(0, index), property.substring(index + 1));
            }

            fieldNumber++;
        }
    }

    public String getGraylogHost() {
        return graylogHost;
    }

    public void setGraylogHost(String graylogHost) {
        this.graylogHost = graylogHost;
    }

    public String getOriginHost() {
        if (null == originHost) {
            originHost = RuntimeContainer.FQDN_HOSTNAME;
        }
        return originHost;
    }

    public void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public int getGraylogPort() {
        return graylogPort;
    }

    public void setGraylogPort(int graylogPort) {
        this.graylogPort = graylogPort;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public boolean isExtractStackTrace() {
        return extractStackTrace;
    }

    public void setExtractStackTrace(boolean extractStackTrace) {
        this.extractStackTrace = extractStackTrace;
    }

    public boolean isFilterStackTrace() {
        return filterStackTrace;
    }

    public void setFilterStackTrace(boolean filterStackTrace) {
        this.filterStackTrace = filterStackTrace;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }

    public void setTimestampPattern(String timestampPattern) {
        this.timestampPattern = timestampPattern;
    }

    public int getMaximumMessageSize() {
        return maximumMessageSize;
    }

    public void setMaximumMessageSize(int maximumMessageSize) {
        this.maximumMessageSize = maximumMessageSize;
    }
}
