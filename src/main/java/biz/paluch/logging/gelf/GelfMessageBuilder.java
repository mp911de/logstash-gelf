package biz.paluch.logging.gelf;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Builder to create a GelfMessage.
 * 
 * @author Mark Paluch
 * @since 18.07.14 20:52
 */
public class GelfMessageBuilder {

    protected String version = GelfMessage.GELF_VERSION;
    protected String host;
    protected String shortMessage;
    protected String fullMessage;
    protected long javaTimestamp;
    protected String level;
    protected String facility = GelfMessage.DEFAULT_FACILITY;
    protected int maximumMessageSize = GelfMessage.DEFAULT_MESSAGE_SIZE;
    protected Map<String, String> additionalFields = new HashMap<String, String>();
    protected Map<String, String> additionalFieldTypes = new HashMap<String, String>();
    protected Map<Pattern, String> dynamicMdcFieldTypes = new HashMap<Pattern, String>();

    protected GelfMessageBuilder() {
    }

    /**
     * Creates a new instance of the GelfMessageBuilder.
     * 
     * @return GelfMessageBuilder
     */
    public static GelfMessageBuilder newInstance() {
        return new GelfMessageBuilder();
    }

    /**
     * Set the version.
     * 
     * @param version the version
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Set the host.
     * 
     * @param host the host
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Set the short_message.
     * 
     * @param shortMessage the short_message
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
        return this;
    }

    /**
     * Set the full_message.
     * 
     * @param fullMessage the fullMessage
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
        return this;
    }

    /**
     * Set the level (severity).
     * 
     * @param level the level
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withLevel(String level) {
        this.level = level;
        return this;
    }

    /**
     * Set the facility.
     * 
     * @param facility the facility
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withFacility(String facility) {
        this.facility = facility;
        return this;
    }

    /**
     * Set the max message size.
     * 
     * @param maximumMessageSize the maximumMessageSize
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withMaximumMessageSize(int maximumMessageSize) {
        this.maximumMessageSize = maximumMessageSize;
        return this;
    }

    /**
     * Set the java timestamp (millis).
     * 
     * @param javaTimestamp the javaTimestamp
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withJavaTimestamp(long javaTimestamp) {
        this.javaTimestamp = javaTimestamp;
        return this;
    }

    /**
     * Add additional fields.
     * 
     * @param additionalFields the additionalFields
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withFields(Map<String, String> additionalFields) {
        this.additionalFields.putAll(additionalFields);
        return this;
    }

    /**
     * Add an additional field.
     * 
     * @param key the key
     * @param value the value
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withField(String key, String value) {
        this.additionalFields.put(key, value);
        return this;
    }

    /**
     * Set additional field types
     * 
     * @param additionalFieldTypes the type map
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withAdditionalFieldTypes(Map<String, String> additionalFieldTypes) {
        this.additionalFieldTypes.putAll(additionalFieldTypes);
        return this;
    }


    /**
     * Set dynamic mdc field types
     *
     * @param dynamicMdcFiledTypes the type map
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withDynamicMdcFieldTypes(Map<Pattern, String> dynamicMdcFiledTypes) {
        this.dynamicMdcFieldTypes.putAll(dynamicMdcFiledTypes);
        return this;
    }

    /**
     * Build a new Gelf message based on the builder settings.
     * 
     * @return GelfMessage
     */
    public GelfMessage build() {

        GelfMessage gelfMessage = new GelfMessage(shortMessage, fullMessage, javaTimestamp, level);
        gelfMessage.addFields(additionalFields);
        gelfMessage.setMaximumMessageSize(maximumMessageSize);
        gelfMessage.setVersion(version);
        gelfMessage.setHost(host);
        gelfMessage.setJavaTimestamp(javaTimestamp);
        gelfMessage.setFacility(facility);
        gelfMessage.setAdditionalFieldTypes(additionalFieldTypes);
        gelfMessage.setDynamicMdcFieldTypes(dynamicMdcFieldTypes);

        return gelfMessage;
    }

}
