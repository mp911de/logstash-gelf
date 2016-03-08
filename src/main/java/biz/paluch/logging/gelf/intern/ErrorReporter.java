package biz.paluch.logging.gelf.intern;

/**
 * Error reporter to report errors while submitting log event.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.01.14 09:18
 */
public interface ErrorReporter {

    /**
     * Report an error caused by a exception.
     *
     * @param message the message
     * @param e the exception
     */
    void reportError(String message, Exception e);
}
