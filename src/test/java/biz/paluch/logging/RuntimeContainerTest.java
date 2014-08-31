package biz.paluch.logging;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RuntimeContainerTest {

    @Test
    public void testMain() throws Exception {
        RuntimeContainer.main(new String[0]);

    }

    @Test
    public void testDifferentOrder() throws Exception {

        System.setProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER,
                RuntimeContainerProperties.RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK);
        RuntimeContainer.lookupHostname(null);

        System.setProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER,
                RuntimeContainerProperties.RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK);

        RuntimeContainer.lookupHostname(null);

        System.clearProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER);
    }

    @Test
    public void testNoLookup() throws Exception {

        System.setProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION, "true");
        RuntimeContainer.lookupHostname(null);

        assertEquals("", RuntimeContainer.ADDRESS);
        assertEquals("unknown", RuntimeContainer.HOSTNAME);
        assertEquals("unknown", RuntimeContainer.FQDN_HOSTNAME);

        System.clearProperty(RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION);
    }
}
