package biz.paluch.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RuntimeContainerUnitTests {

    @Test
    void testMain() throws Exception {
        RuntimeContainer.main(new String[0]);
    }

    @Test
    void testDifferentOrder() throws Exception {

        System.setProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER,
                RuntimeContainerProperties.RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK);
        RuntimeContainer.lookupHostname(null);

        System.setProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER,
                RuntimeContainerProperties.RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK);

        RuntimeContainer.lookupHostname(null);

        System.clearProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER);
    }

    @Test
    void testNoLookup() throws Exception {

        System.setProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION, "true");
        RuntimeContainer.lookupHostname(null);

        assertThat(RuntimeContainer.ADDRESS).isEqualTo("");
        assertThat(RuntimeContainer.HOSTNAME).isEqualTo("unknown");
        assertThat(RuntimeContainer.FQDN_HOSTNAME).isEqualTo("unknown");

        System.clearProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION);
    }
}
