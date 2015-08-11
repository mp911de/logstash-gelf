package biz.paluch.logging.gelf.intern;

import java.io.Closeable;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public interface GelfSender extends Closeable {

    /**
     * Send the Gelf message.
     * 
     * @param message the message
     * @return {@literal true} if the message was sent
     */
    boolean sendMessage(GelfMessage message);

    /**
     * Close the sender and free resources.
     */
    void close();
}
