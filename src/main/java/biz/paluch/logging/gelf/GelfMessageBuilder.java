package biz.paluch.logging.gelf;

import java.util.HashMap;
import java.util.Map;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Builder to create a GelfMessage.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 18.07.14 20:52
 */
public class GelfMessageBuilder {

    private String version = GelfMessage.GELF_VERSION;
    private String host;
    private String shortMessage;
    private String fullMessage;
    private long javaTimestamp;
    private String level;
    private String facility = GelfMessage.DEFAULT_FACILITY;
    private Map<String, String> additonalFields = new HashMap<String, String>();
    private int maximumMessageSize = GelfMessage.DEFAULT_MESSAGE_SIZE;

    private GelfMessageBuilder() {

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
     * @param additonalFields the additonalFields
     * @return GelfMessageBuilder
     */
    public GelfMessageBuilder withFields(Map<String, String> additonalFields) {
        this.additonalFields.putAll(additonalFields);
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
        this.additonalFields.put(key, value);
        return this;
    }

    /**
     * Build a new Gelf message based on the builder settings.
     * 
     * @return GelfMessage
     */
    public GelfMessage build() {

        GelfMessage gelfMessage = new GelfMessage(shortMessage, fullMessage, javaTimestamp, level);
        gelfMessage.addFields(additonalFields);
        gelfMessage.setMaximumMessageSize(maximumMessageSize);
        gelfMessage.setVersion(version);
        gelfMessage.setHost(host);
        gelfMessage.setJavaTimestamp(javaTimestamp);
        gelfMessage.setFacility(facility);
        gelfMessage.setFacility(facility);

        return gelfMessage;
    }

}
