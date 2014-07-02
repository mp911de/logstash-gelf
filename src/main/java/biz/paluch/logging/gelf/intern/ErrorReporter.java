package biz.paluch.logging.gelf.intern;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.01.14 09:18
 */
public interface ErrorReporter {

    /**
     * Report an error caused by a exception.
     * 
     * @param message
     * @param e
     */
    void reportError(String message, Exception e);
}
