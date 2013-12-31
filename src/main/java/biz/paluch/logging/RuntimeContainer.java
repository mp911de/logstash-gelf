package biz.paluch.logging;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * Static Details about the runtime container: Hostname (simple/fqdn), Address and timestamp of the first access (time when the
 * application was loaded).
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RuntimeContainer {

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

    private static final Logger LOGGER = Logger.getLogger(RuntimeContainer.class);

    /**
     * Utility Constructor.
     */
    private RuntimeContainer() {

    }

    static {

        FIRST_ACCESS = System.currentTimeMillis();

        String myHostName = "unknown";
        String myFQDNHostName = "unknown";
        String myAddress = "";
        try {

            InetAddress inetAddress = getInetAddressWithHostname();

            if (inetAddress == null) {
                inetAddress = InetAddress.getLocalHost();
            }

            myHostName = getHostname(inetAddress, false);
            myFQDNHostName = getHostname(inetAddress, true);
            myAddress = inetAddress.getHostAddress();
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
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

                if (inetAddress.isLoopbackAddress()) {
                    continue;
                }

                if (inetAddress.getHostAddress().equals(inetAddress.getCanonicalHostName())) {
                    continue;
                }

                return inetAddress;
            }
        }

        return null;
    }
}
