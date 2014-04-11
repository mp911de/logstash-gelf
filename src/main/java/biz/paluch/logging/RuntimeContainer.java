package biz.paluch.logging;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Static Details about the runtime container: Hostname (simple/fqdn), Address and timestamp of the first access (time when the
 * application was loaded).
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RuntimeContainer {

    public static final String PROPERTY_LOGSTASH_GELF_HOSTNAME = "logstash-gelf.hostname";
    public static final String PROPERTY_LOGSTASH_GELF_FQDN_HOSTNAME = "logstash-gelf.fqdn.hostname";
    public static final String PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION = "logstash-gelf.skipHostnameResolution";
    public static final String PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER = "logstash-gelf.resolutionOrder";
    public static final String RESOLUTION_ORDER_LOCALHOST_NETWORK_FALLBACK = "localhost,network";
    public static final String RESOLUTION_ORDER_NETWORK_LOCALHOST_FALLBACK = "network,localhost";

    /**
     * Current Hostname.
     */
    public static final String HOSTNAME;

    /**
     * Current FQDN Hostname.
     */
    public static final String FQDN_HOSTNAME;

    /**
     * Current Address.
     */
    public static final String ADDRESS;

    /**
     * Load-Time of this class.
     */
    public static final long FIRST_ACCESS;

    /**
     * Utility Constructor.
     */
    private RuntimeContainer() {

    }

    static {

        FIRST_ACCESS = System.currentTimeMillis();

        String myHostName = System.getProperty(PROPERTY_LOGSTASH_GELF_HOSTNAME, "unknown");
        String myFQDNHostName = System.getProperty(PROPERTY_LOGSTASH_GELF_FQDN_HOSTNAME, "unknown");
        String myAddress = "";

        if (!Boolean.getBoolean(PROPERTY_LOGSTASH_GELF_SKIP_HOSTNAME_RESOLUTION)) {

            try {

                String resolutionOrder = System.getProperty(PROPERTY_LOGSTASH_GELF_HOSTNAME_RESOLUTION_ORDER,
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
                System.err.print("Cannot resolve hostname");
                e.printStackTrace(System.err);
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
