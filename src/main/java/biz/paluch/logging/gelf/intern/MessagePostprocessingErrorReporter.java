package biz.paluch.logging.gelf.intern;

/**
 * {@link ErrorReporter} that post-processes the error message if it is {@code null} by using the exception class name as
 * fallback.
 *
 * @author Mark Paluch
 * @since 1.11.2
 */
public class MessagePostprocessingErrorReporter implements ErrorReporter {

    private final ErrorReporter delegate;

    public MessagePostprocessingErrorReporter(ErrorReporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void reportError(String message, Exception e) {

        String messageToUse = postProcessMessage(message, e);

        delegate.reportError(messageToUse, e);
    }

    private static String postProcessMessage(String message, Exception e) {

        if ((message == null || "null".equalsIgnoreCase(message)) && e != null) {

            if (e.getMessage() != null) {
                return e.getMessage();
            }

            return e.getClass().getSimpleName();
        }

        return message;
    }
}
