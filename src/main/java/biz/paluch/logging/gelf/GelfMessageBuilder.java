package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.intern.GelfMessage;

import java.util.HashMap;
import java.util.Map;

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

    public GelfMessageBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public GelfMessageBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public GelfMessageBuilder withShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
        return this;
    }

    public GelfMessageBuilder withFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
        return this;
    }

    public GelfMessageBuilder withLevel(String level) {
        this.level = level;
        return this;
    }

    public GelfMessageBuilder withFacility(String facility) {
        this.facility = facility;
        return this;
    }

    public GelfMessageBuilder withMaximumMessageSize(int maximumMessageSize) {
        this.maximumMessageSize = maximumMessageSize;
        return this;
    }

    public GelfMessageBuilder withJavaTimestamp(long javaTimestamp) {
        this.javaTimestamp = javaTimestamp;
        return this;
    }

    public GelfMessageBuilder withAdditionalFields(Map<String, String> additonalFields) {
        this.additonalFields.putAll(additonalFields);
        return this;
    }

    public GelfMessageBuilder withAdditionalField(String key, String value) {
        this.additonalFields.put(key, value);
        return this;
    }

    public GelfMessage build() {

        GelfMessage gelfMessage = new GelfMessage(shortMessage, fullMessage, javaTimestamp, level);
        gelfMessage.addFields(additonalFields);
        gelfMessage.setMaximumMessageSize(maximumMessageSize);
        gelfMessage.setVersion(version);
        gelfMessage.setHost(host);
        gelfMessage.setJavaTimestamp(javaTimestamp);
        gelfMessage.setFacility(facility);

        return gelfMessage;
    }

}
