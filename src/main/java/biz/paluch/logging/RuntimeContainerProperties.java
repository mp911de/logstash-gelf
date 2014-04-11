package biz.paluch.logging;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 11.04.14 12:30
 */
public class RuntimeContainerProperties {
    public static final String PROPERTY_LOGSTASH_GELF_HOSTNAME = "logstash-gelf.hostname";
    public static final String PROPERTY_LOGSTASH_GELF_FQDN_HOSTNAME = "logstash-gelf.fqdn.hostname";
    public static final String PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION = "logstash-gelf.skipHostnameResolution";
    public static final String PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER = "logstash-gelf.resolutionOrder";
    public static final String RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK = "localhost,network";
    public static final String RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK = "network,localhost";
    private RuntimeContainerProperties() {

    }
}
