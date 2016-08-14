package biz.paluch.logging.gelf.intern;

import biz.paluch.logging.gelf.GelfMessageBuilder;

/**
 * @author Mark Paluch
 */
public class PoolingGelfMessageBuilder extends GelfMessageBuilder {

    private PoolingGelfMessageBuilder() {
    }

    /**
     * Creates a new instance of the GelfMessageBuilder.
     *
     * @return GelfMessageBuilder
     */
    public static PoolingGelfMessageBuilder newInstance() {
        return new PoolingGelfMessageBuilder();
    }

    /**
     * Recycle this {@link GelfMessageBuilder} to a default state.
     *
     * @return {@code this} {@link GelfMessageBuilder}
     */
    public GelfMessageBuilder recycle() {

        version = GelfMessage.GELF_VERSION;
        host = null;
        shortMessage = null;
        fullMessage = null;
        javaTimestamp = 0;
        level = null;
        facility = GelfMessage.DEFAULT_FACILITY;
        maximumMessageSize = GelfMessage.DEFAULT_MESSAGE_SIZE;

        additionalFields.clear();
        additionalFieldTypes.clear();

        return this;
    }

    /**
     * Build a new Gelf message based on the builder settings.
     *
     * @return GelfMessage
     */
    public GelfMessage build() {

        GelfMessage gelfMessage = new PoolingGelfMessage(shortMessage, fullMessage, javaTimestamp, level);
        gelfMessage.addFields(additionalFields);
        gelfMessage.setMaximumMessageSize(maximumMessageSize);
        gelfMessage.setVersion(version);
        gelfMessage.setHost(host);
        gelfMessage.setJavaTimestamp(javaTimestamp);
        gelfMessage.setFacility(facility);
        gelfMessage.setAdditionalFieldTypes(additionalFieldTypes);

        return gelfMessage;
    }
}
