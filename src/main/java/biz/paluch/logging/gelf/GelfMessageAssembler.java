package biz.paluch.logging.gelf;

import static biz.paluch.logging.gelf.GelfMessageBuilder.newInstance;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.RuntimeContainerProperties;
import biz.paluch.logging.StackTraceFilter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.HostAndPortProvider;
import biz.paluch.logging.gelf.intern.PoolingGelfMessageBuilder;

/**
 * Creates {@link GelfMessage} based on various {@link LogEvent}. A {@link LogEvent} encapsulates log-framework specifics and
 * exposes commonly used details of log events.
 *
 * @author Mark Paluch
 * @since 26.09.13 15:05
 */
public class GelfMessageAssembler implements HostAndPortProvider {

    public static final String PROPERTY_USE_POOLING = "logstash-gelf.message.pooling";
    private static final boolean USE_POOLING = Boolean
            .valueOf(RuntimeContainerProperties.getProperty(PROPERTY_USE_POOLING, "true"));

    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int MAX_MESSAGE_SIZE = Integer.MAX_VALUE;

    public static final String FIELD_MESSAGE_PARAM = "MessageParam";
    public static final String FIELD_STACK_TRACE = "StackTrace";

    private String host;
    private String version = GelfMessage.GELF_VERSION;
    private String originHost;
    private int port;
    private String facility;
    private boolean includeLogMessageParameters = true;
    private StackTraceExtraction stackTraceExtraction = StackTraceExtraction.OFF;
    private int maximumMessageSize = 8192;

    private List<MessageField> fields = new ArrayList<MessageField>();
    private Map<String, String> additionalFieldTypes = new HashMap<String, String>();

    private String timestampPattern = "yyyy-MM-dd HH:mm:ss,SSSS";

    private boolean usePooling = USE_POOLING;

    private ThreadLocal<PoolingGelfMessageBuilder> builders = new ThreadLocal<PoolingGelfMessageBuilder>() {

        @Override
        protected PoolingGelfMessageBuilder initialValue() {
            return PoolingGelfMessageBuilder.newInstance();
        }
    };

    public GelfMessageAssembler() {
    }

    /**
     * Initialize the {@link GelfMessageAssembler} from a property provider.
     *
     * @param propertyProvider property provider to obtain configuration properties
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

        if (port != null && !"".equals(port)) {
            this.port = Integer.parseInt(port);
        }

        originHost = propertyProvider.getProperty(PropertyProvider.PROPERTY_ORIGIN_HOST);
        setExtractStackTrace(propertyProvider.getProperty(PropertyProvider.PROPERTY_EXTRACT_STACKTRACE));
        setFilterStackTrace(
                "true".equalsIgnoreCase(propertyProvider.getProperty(PropertyProvider.PROPERTY_FILTER_STACK_TRACE)));

        String includeLogMessageParameters = propertyProvider
                .getProperty(PropertyProvider.PROPERTY_INCLUDE_LOG_MESSAGE_PARAMETERS);
        if (includeLogMessageParameters != null && !includeLogMessageParameters.trim().equals("")) {
            setIncludeLogMessageParameters("true".equalsIgnoreCase(includeLogMessageParameters));
        }

        setupStaticFields(propertyProvider);
        setupAdditionalFieldTypes(propertyProvider);
        facility = propertyProvider.getProperty(PropertyProvider.PROPERTY_FACILITY);
        String version = propertyProvider.getProperty(PropertyProvider.PROPERTY_VERSION);

        if (version != null && !"".equals(version)) {
            this.version = version;
        }

        String messageSize = propertyProvider.getProperty(PropertyProvider.PROPERTY_MAX_MESSAGE_SIZE);
        if (messageSize != null) {
            maximumMessageSize = Integer.parseInt(messageSize);
        }
    }

    /**
     * Produce a {@link GelfMessage}.
     *
     * @param logEvent the log event
     * @return a new GelfMessage
     */
    public GelfMessage createGelfMessage(LogEvent logEvent) {

        GelfMessageBuilder builder = usePooling ? builders.get().recycle() : newInstance();

        Throwable throwable = logEvent.getThrowable();
        String message = logEvent.getMessage();

        if (GelfMessage.isEmpty(message) && throwable != null) {
            message = throwable.toString();
        }

        String shortMessage = message;
        if (message.length() > MAX_SHORT_MESSAGE_LENGTH) {
            shortMessage = message.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
        }

        builder.withShortMessage(shortMessage).withFullMessage(message).withJavaTimestamp(logEvent.getLogTimestamp());
        builder.withLevel(logEvent.getSyslogLevel());
        builder.withVersion(getVersion());
        builder.withAdditionalFieldTypes(additionalFieldTypes);

        for (MessageField field : fields) {
            Values values = getValues(logEvent, field);
            if (values == null || !values.hasValues()) {
                continue;
            }

            for (String entryName : values.getEntryNames()) {
                String value = values.getValue(entryName);
                if (value == null) {
                    continue;

                }
                builder.withField(entryName, value);
            }
        }

        if (stackTraceExtraction.isEnabled() && throwable != null) {
            addStackTrace(throwable, builder);
        }

        if (includeLogMessageParameters && logEvent.getParameters() != null) {
            for (int i = 0; i < logEvent.getParameters().length; i++) {
                Object param = logEvent.getParameters()[i];
                builder.withField(FIELD_MESSAGE_PARAM + i, "" + param);
            }
        }

        builder.withHost(getOriginHost());

        if (null != facility) {
            builder.withFacility(facility);
        }

        builder.withMaximumMessageSize(maximumMessageSize);
        return builder.build();
    }

    private Values getValues(LogEvent logEvent, MessageField field) {

        if (field instanceof StaticMessageField) {
            return new Values(field.getName(), getValue((StaticMessageField) field));
        }

        if (field instanceof LogMessageField) {
            LogMessageField logMessageField = (LogMessageField) field;
            if (logMessageField.getNamedLogField() == LogMessageField.NamedLogField.Time) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(timestampPattern);
                return new Values(field.getName(), dateFormat.format(new Date(logEvent.getLogTimestamp())));
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

    private void addStackTrace(Throwable thrown, GelfMessageBuilder builder) {
        if (stackTraceExtraction.isFilter()) {
            builder.withField(FIELD_STACK_TRACE, StackTraceFilter.getFilteredStackTrace(thrown, stackTraceExtraction.getRef()));
        } else {
            final StringWriter sw = new StringWriter();
            StackTraceFilter.getThrowable(thrown, stackTraceExtraction.getRef()).printStackTrace(new PrintWriter(sw));
            builder.withField(FIELD_STACK_TRACE, sw.toString());
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

    private void setupAdditionalFieldTypes(PropertyProvider propertyProvider) {
        int fieldNumber = 0;
        while (true) {
            final String property = propertyProvider.getProperty(PropertyProvider.PROPERTY_ADDITIONAL_FIELD_TYPE + fieldNumber);
            if (null == property) {
                break;
            }
            final int index = property.indexOf('=');
            if (-1 != index) {

                String field = property.substring(0, index);
                String type = property.substring(index + 1);
                setAdditionalFieldType(field, type);
            }

            fieldNumber++;
        }
    }

    public void setAdditionalFieldType(String field, String type) {
        additionalFieldTypes.put(field, type);
    }

    public void addField(MessageField field) {
        if (!fields.contains(field)) {
            this.fields.add(field);
        }
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
        if (port > MAX_PORT_NUMBER || port < 1) {
            throw new IllegalArgumentException("Invalid port number: " + port + ", supported range: 1-" + MAX_PORT_NUMBER);
        }
        this.port = port;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public boolean isExtractStackTrace() {
        return stackTraceExtraction.isEnabled();
    }

    public String getExtractStackTrace() {

        if (stackTraceExtraction.isEnabled()) {

            if (stackTraceExtraction.getRef() == 0) {
                return "true";
            }
            return Integer.toString(stackTraceExtraction.getRef());
        }

        return "false";
    }

    public void setExtractStackTrace(boolean extractStackTrace) {
        this.stackTraceExtraction = stackTraceExtraction.applyExtaction("" + extractStackTrace);
    }

    public void setExtractStackTrace(String value) {
        this.stackTraceExtraction = stackTraceExtraction.applyExtaction(value);
    }

    public boolean isFilterStackTrace() {
        return stackTraceExtraction.isEnabled();
    }

    public void setFilterStackTrace(boolean filterStackTrace) {
        this.stackTraceExtraction = stackTraceExtraction.applyFilter(filterStackTrace);
    }

    public boolean isIncludeLogMessageParameters() {
        return includeLogMessageParameters;
    }

    public void setIncludeLogMessageParameters(boolean includeLogMessageParameters) {
        this.includeLogMessageParameters = includeLogMessageParameters;
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

        if (maximumMessageSize > MAX_MESSAGE_SIZE || maximumMessageSize < 1) {
            throw new IllegalArgumentException(
                    "Invalid maximum message size: " + maximumMessageSize + ", supported range: 1-" + MAX_MESSAGE_SIZE);
        }

        this.maximumMessageSize = maximumMessageSize;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {

        if (!GelfMessage.GELF_VERSION_1_0.equals(version) && !GelfMessage.GELF_VERSION_1_1.equals(version)) {
            throw new IllegalArgumentException("Invalid GELF version: " + version + ", supported range: "
                    + GelfMessage.GELF_VERSION_1_0 + ", " + GelfMessage.GELF_VERSION_1_1);
        }

        this.version = version;
    }

    static class StackTraceExtraction {

        private static final StackTraceExtraction OFF = new StackTraceExtraction(false, false, 0);
        private static final StackTraceExtraction ON = new StackTraceExtraction(true, false, 0);
        private static final StackTraceExtraction FILTERED = new StackTraceExtraction(true, true, 0);
        private final boolean enabled;
        private final boolean filter;
        private final int ref;

        private StackTraceExtraction(boolean enabled, boolean filter, int ref) {
            this.enabled = enabled;
            this.filter = filter;
            this.ref = ref;
        }

        /**
         * Parse the stack trace filtering value.
         * 
         * @param value
         * @return
         */
        public static StackTraceExtraction from(String value, boolean filter) {

            if (value == null) {
                return OFF;
            }

            boolean enabled = Boolean.parseBoolean(value);

            int ref = 0;
            if (!value.equalsIgnoreCase("false") && !value.trim().isEmpty()) {
                try {
                    ref = Integer.parseInt(value);
                    enabled = true;
                } catch (NumberFormatException e) {
                    ref = 0;
                }
            }

            return new StackTraceExtraction(enabled, filter, ref);
        }

        public StackTraceExtraction applyExtaction(String value) {

            StackTraceExtraction parsed = from(value, isFilter());
            return new StackTraceExtraction(parsed.isEnabled(), parsed.isFilter(), parsed.getRef());
        }

        public StackTraceExtraction applyFilter(boolean filterStackTrace) {
            return new StackTraceExtraction(isEnabled(), filterStackTrace, getRef());
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isFilter() {
            return filter;
        }

        public int getRef() {
            return ref;
        }
    }
}
