package biz.paluch.logging.gelf.intern;

import biz.paluch.logging.RuntimeContainerProperties;
import biz.paluch.logging.gelf.GelfMessageBuilder;

/**
 * @author Mark Paluch
 */
public class PoolingGelfMessageBuilder extends GelfMessageBuilder {

    /**
     * Can be
     * <ul>
     * <li>{@literal static} (default value) for static held pools</li>
     * <li>{@literal true} for using instance-based held pools</li>
     * <li>{@literal false} to disable pooling</li>
     * </ul>
     */
    public static final String PROPERTY_USE_POOLING = "logstash-gelf.message.pooling";

    private static final String USE_POOLING_VAL = RuntimeContainerProperties.getProperty(PROPERTY_USE_POOLING, "static");
    private static final boolean STATIC_POOLING = USE_POOLING_VAL.equalsIgnoreCase("static");
    private static final PoolHolder STATIC_POOL_HOLDER = STATIC_POOLING ? PoolHolder.threadLocal() : PoolHolder.noop();

    private final PoolHolder poolHolder;

    private PoolingGelfMessageBuilder(PoolHolder poolHolder) {
        this.poolHolder = poolHolder;
    }

    /**
     * Creates a new instance of the GelfMessageBuilder.
     *
     * @return GelfMessageBuilder
     */
    public static PoolingGelfMessageBuilder newInstance() {
        return new PoolingGelfMessageBuilder(STATIC_POOLING ? STATIC_POOL_HOLDER : PoolHolder.threadLocal());
    }

    /**
     * @return {@literal true} if pooling (static/instance-held pools) is enabled.
     */
    public static boolean usePooling() {
        return STATIC_POOLING || USE_POOLING_VAL.equalsIgnoreCase("true");
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

        GelfMessage gelfMessage = new PoolingGelfMessage(shortMessage, fullMessage, javaTimestamp, level, poolHolder);
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
