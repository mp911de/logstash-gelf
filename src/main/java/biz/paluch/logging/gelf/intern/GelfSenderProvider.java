package biz.paluch.logging.gelf.intern;

import java.io.IOException;

/**
 *
 * @author https://github.com/Batigoal/logstash-gelf.git
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface GelfSenderProvider {

    /**
     *
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
