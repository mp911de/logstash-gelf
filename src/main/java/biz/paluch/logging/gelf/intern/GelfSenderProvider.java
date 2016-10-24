package biz.paluch.logging.gelf.intern;

import java.io.IOException;

/**
 * Strategy interface to create a {@link GelfSender}. Implementations decide based on the {@code host} string whether they
 * support {@link GelfSender} creation.
 * 
 * @author https://github.com/Batigoal/logstash-gelf.git
 * @author Mark Paluch
 */
public interface GelfSenderProvider {

    /**
     * @param host the host string
     * @return true if the host scheme/pattern/uri is supported by this provider.
     */
    boolean supports(String host);

    /**
     * Create the sender based on the passed configuration.
     *
     * @param configuration the sender configuration
     * @return GelfSender instance.
     * @throws IOException if there is an error in the underlying protocol
     */
    GelfSender create(GelfSenderConfiguration configuration) throws IOException;
}
