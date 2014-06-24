package biz.paluch.logging.gelf.intern;

import java.io.IOException;

/**
 * 
 * (c) https://github.com/Batigoal/logstash-gelf.git
 * 
 */
public interface GelfSenderProvider {

    /**
     * 
     * @param host
     * @return true if the host scheme/pattern/uri is supported by this provider.
     */
    boolean supports(String host);

    /**
     * Create the sender based on the passed configuration.
     * 
     * @param configuration
     * @return GelfSender instance.
     * @throws IOException
     */
    GelfSender create(GelfSenderConfiguration configuration) throws IOException;
}
