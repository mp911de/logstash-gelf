package biz.paluch.logging.gelf;

/**
 * Provides access to Log-Framework properties.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:04
 */
public interface PropertyProvider {

    /**
     * @deprecated Use host.
     */
    @Deprecated
    String PROPERTY_GRAYLOG_HOST = "graylogHost";

    /**
     * @deprecated use port.
     */
    @Deprecated
    String PROPERTY_GRAYLOG_PORT = "graylogPort";

    String PROPERTY_HOST = "host";
    String PROPERTY_PORT = "port";

    String PROPERTY_ORIGIN_HOST = "originHost";
    String PROPERTY_EXTRACT_STACKTRACE = "extractStackTrace";
    String PROPERTY_FILTER_STACK_TRACE = "filterStackTrace";
    String PROPERTY_FACILITY = "facility";
    String PROPERTY_MAX_MESSAGE_SIZE = "maximumMessageSize";
    String PROPERTY_ADDITIONAL_FIELD = "additionalField.";
    String PROPERTY_ADDITIONAL_FIELDS = "additionalFields";

    String PROPERTY_FILTER = "filter";
    String PROPERTY_LEVEL = "level";
    String PROPERTY_VERSION = "version";

    String getProperty(String propertyName);
}
