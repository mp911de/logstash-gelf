package biz.paluch.logging.gelf;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.StackTraceFilter;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:05
 */
public class GelfMessageAssembler {

    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;

    public static final String FIELD_MESSAGE_PARAM = "MessageParam";
    public static final String FIELD_STACK_TRACE = "StackTrace";

    private String host;
    private String originHost;
    private int port = 12201;
    private String facility;
    private boolean extractStackTrace;
    private boolean filterStackTrace;
    private int maximumMessageSize = 8192;

    private List<MessageField> fields = new ArrayList<MessageField>();

    private String timestampPattern = "yyyy-MM-dd HH:mm:ss,SSSS";

    /**
     * Initialize datastructure from property provider.
     * 
     * @param propertyProvider
     */
    public void initialize(PropertyProvider propertyProvider) {
        host = propertyProvider.getProperty(PropertyProvider.PROPERTY_HOST);
        if (host == null) {
            host = propertyProvider.getProperty(PropertyProvider.PROPERTY_GRAYLOG_HOST);
        }

        String port = propertyProvider.getProperty(PropertyProvider.PROPERTY_PORT);
        if (port == null) {
            port = propertyProvider.getProperty(PropertyProvider.PROPERTY_GRAYLOG_PORT);
        }

        if (port != null) {
            this.port = Integer.parseInt(port);
        }

        originHost = propertyProvider.getProperty(PropertyProvider.PROPERTY_ORIGIN_HOST);
        extractStackTrace = "true".equalsIgnoreCase(propertyProvider.getProperty(PropertyProvider.PROPERTY_EXTRACT_STACKTRACE));
        filterStackTrace = "true".equalsIgnoreCase(propertyProvider.getProperty(PropertyProvider.PROPERTY_FILTER_STACK_TRACE));

        setupStaticFields(propertyProvider);
        facility = propertyProvider.getProperty(PropertyProvider.PROPERTY_FACILITY);

        String messageSize = propertyProvider.getProperty(PropertyProvider.PROPERTY_MAX_MESSAGE_SIZE);
        if (messageSize != null) {
            maximumMessageSize = Integer.parseInt(messageSize);
        }
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

        for (MessageField field : fields) {
            Values values = getValues(logEvent, field);
            if (values == null || !values.hasValues()) {
                continue;
            }

            for (String entryName : values.getEntryNames())
            {
                String value = values.getValue(entryName);
                if(value == null)
                {
                    continue;

                }
                gelfMessage.addField(entryName, value);
            }
        }

        if (extractStackTrace) {
            addStackTrace(logEvent, gelfMessage);
        }

        if (logEvent.getParameters() != null) {
            for (int i = 0; i < logEvent.getParameters().length; i++) {
                Object param = logEvent.getParameters()[i];
                gelfMessage.addField(FIELD_MESSAGE_PARAM + i, "" + param);
            }
        }

        gelfMessage.setHost(getOriginHost());

        if (null != facility) {
            gelfMessage.setFacility(facility);
        }

        gelfMessage.setMaximumMessageSize(maximumMessageSize);
        return gelfMessage;
    }

    private Values getValues(LogEvent logEvent, MessageField field) {

        if (field instanceof StaticMessageField) {
            return new Values(field.getName(),getValue((StaticMessageField) field));
        }

        if (field instanceof LogMessageField) {
            LogMessageField logMessageField = (LogMessageField) field;
            if (logMessageField.getNamedLogField() == LogMessageField.NamedLogField.Time) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(timestampPattern);
                return new Values(field.getName(),dateFormat.format(new Date(logEvent.getLogTimestamp())));
            }

            if (logMessageField.getNamedLogField() == LogMessageField.NamedLogField.Server) {
                return new Values(field.getName(), getOriginHost());
            }
        }

        return logEvent.getValues(field);
    }

    private String getValue(StaticMessageField field) {
        return field.getValue();
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
        while (true) {
            final String property = propertyProvider.getProperty(PropertyProvider.PROPERTY_ADDITIONAL_FIELD + fieldNumber);
            if (null == property) {
                break;
            }
            final int index = property.indexOf('=');
            if (-1 != index) {

                StaticMessageField field = new StaticMessageField(property.substring(0, index), property.substring(index + 1));
                addField(field);
            }

            fieldNumber++;
        }
    }

    public void addField(MessageField field) {
        this.fields.add(field);
    }

    public void addFields(Collection<? extends MessageField> fields) {
        this.fields.addAll(fields);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
