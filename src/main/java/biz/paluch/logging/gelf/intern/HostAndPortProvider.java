package biz.paluch.logging.gelf.intern;

/**
 * Provided a host and a port number.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 18.07.14 20:53
 */
public interface HostAndPortProvider {

    /**
     * 
     * @return the host.
     */
    String getHost();

    /**
     * 
     * @return the port number.
     */
    int getPort();
}
