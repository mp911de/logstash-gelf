package biz.paluch.logging.gelf.intern;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public interface GelfSender {

    /**
     * Send the Gelf message.
     * 
     * @param message
     * @return
     */
    boolean sendMessage(GelfMessage message);

    /**
     * Close the sender and free resources.
     */
    void close();
}
