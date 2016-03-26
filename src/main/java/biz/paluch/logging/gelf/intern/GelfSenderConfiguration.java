package biz.paluch.logging.gelf.intern;

import java.util.Map;

/**
 * Configuration for a Gelf Sender.
 *
 * @author https://github.com/Batigoal/logstash-gelf.git
 * @author Mark Paluch
 */
public interface GelfSenderConfiguration {

    /**
     *
     * @return the host part (can be any arbitrary string which is supported by the GelfSender)
     */
    String getHost();

    /**
     *
     * @return port number (optional, 0 if not provided)
     */
    int getPort();

    /**
     *
     * @return the ErrorReporter to report any errors
     */
    ErrorReporter getErrorReporter();

    /**
     * @return some sender specific configurations
     */
    Map<String, Object> getSpecificConfigurations();
}
