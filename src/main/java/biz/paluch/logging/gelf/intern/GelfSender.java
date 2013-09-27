package biz.paluch.logging.gelf.intern;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public interface GelfSender {
    public static final int DEFAULT_PORT = 12201;

    public boolean sendMessage(GelfMessage message);

    public void close();
}
