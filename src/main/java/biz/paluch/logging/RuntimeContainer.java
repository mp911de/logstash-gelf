package biz.paluch.logging;

import static biz.paluch.logging.RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_FQDN_HOSTNAME;
import static biz.paluch.logging.RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME;
import static biz.paluch.logging.RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER;
import static biz.paluch.logging.RuntimeContainerProperties.PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION;
import static biz.paluch.logging.RuntimeContainerProperties.RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK;
import static biz.paluch.logging.RuntimeContainerProperties.RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK;
import static biz.paluch.logging.RuntimeContainerProperties.getProperty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import biz.paluch.logging.gelf.intern.ErrorReporter;

/**
 * Static Details about the runtime container: Hostname (simple/fqdn), Address and timestamp of the first access (time when the
 * application was loaded).
 * 
 * @author Mark Paluch
 */
public class RuntimeContainer {

    /**
     * Current Hostname.
     */
    public static String HOSTNAME;

    /**
     * Current FQDN Hostname.
     */
    public static String FQDN_HOSTNAME;

    /**
     * Current Address.
     */
    public static String ADDRESS;

    /**
     * Load-Time of this class.
     */
    public static final long FIRST_ACCESS;

    private static boolean initialized;

    /**
     * Utility Constructor.
     */
    private RuntimeContainer() {

    }

    static {
        FIRST_ACCESS = System.currentTimeMillis();
    }

    /**
     * Initialize only once.
     * @param errorReporter the error reporter
     */
    public static void initialize(ErrorReporter errorReporter) {

        if (!initialized) {
            lookupHostname(errorReporter);
            initialized = true;
        }
    }

    /**
     * Triggers the hostname lookup.
     * @param errorReporter the error reporter
     */
    public static void lookupHostname(ErrorReporter errorReporter) {
        String myHostName = getProperty(PROPERTY_LOGSTASH_GELF_HOSTNAME, "unknown");
        String myFQDNHostName = getProperty(PROPERTY_LOGSTASH_GELF_FQDN_HOSTNAME, "unknown");
        String myAddress = "";

        if (!Boolean.parseBoolean(getProperty(PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION, "false"))) {

            try {

                String resolutionOrder = getProperty(PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER,
                        RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK);

                InetAddress inetAddress = null;
                if (resolutionOrder.equals(RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK)) {
                    inetAddress = getInetAddressWithHostname();
                }

                if (resolutionOrder.equals(RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK)) {
                    if (isQualified(InetAddress.getLocalHost())) {
                        inetAddress = InetAddress.getLocalHost();
                    } else {
                        inetAddress = getInetAddressWithHostname();
                    }
                }

                if (inetAddress == null) {
                    inetAddress = InetAddress.getLocalHost();
                }

                myHostName = getHostname(inetAddress, false);
                myFQDNHostName = getHostname(inetAddress, true);
                myAddress = inetAddress.getHostAddress();
            } catch (IOException e) {
                errorReporter.reportError("Cannot resolve hostname", e);
            }
        }

        FQDN_HOSTNAME = myFQDNHostName;
        HOSTNAME = myHostName;
        ADDRESS = myAddress;
    }

    private static String getHostname(InetAddress inetAddress, boolean fqdn) throws IOException {

        String hostname = inetAddress.getHostName();
        if (hostname.indexOf('.') != -1 && !fqdn) {
            hostname = hostname.substring(0, hostname.indexOf('.'));
        }

        return hostname;
    }

    private static InetAddress getInetAddressWithHostname() throws SocketException {
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();

        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> ias = ni.getInetAddresses();
            while (ias.hasMoreElements()) {
                InetAddress inetAddress = ias.nextElement();

                if (!isQualified(inetAddress)) {
                    continue;
                }
                return inetAddress;
            }
        }

        return null;
    }

    private static boolean isQualified(InetAddress inetAddress) {
        return !inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().equals(inetAddress.getCanonicalHostName());
    }

    public static void main(String[] args) {
        System.out.println("Host-Resolution: " + FQDN_HOSTNAME + "/" + HOSTNAME);
    }
}
