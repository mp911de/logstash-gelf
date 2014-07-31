package biz.paluch.logging;

/**
 * Collection of property names in order to control host name/host name resolution.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 11.04.14 12:30
 */
public class RuntimeContainerProperties {
    /**
     * Set this system property to a simple hostname to use a fixed host name.
     */
    public static final String PROPERTY_LOGSTASH_GELF_HOSTNAME = "logstash-gelf.hostname";

    /**
     * Set this system property to a fully qualified hostname to use a fixed host name.
     */
    public static final String PROPERTY_LOGSTASH_GELF_FQDN_HOSTNAME = "logstash-gelf.fqdn.hostname";

    /**
     * Set this system property to <code>true</code> to skip hosname resolution. The string <code>unknown</code> will be used as
     * hostname.
     */
    public static final String PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION = "logstash-gelf.skipHostnameResolution";

    /**
     * Set this propery to {@link #RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK} or
     * {@link #RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK} to control the hostname resolution order.
     */
    public static final String PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER = "logstash-gelf.resolutionOrder";

    /**
     * Resolution order: First inspect the local host name, then try to get the host name from network devices.
     */
    public static final String RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK = "localhost,network";

    /**
     * Resolution order: First inspect the network devices to retrieve a host name, then try to get the host name from the local
     * host.
     */
    public static final String RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK = "network,localhost";

    private RuntimeContainerProperties() {

    }
}
