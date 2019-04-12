package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * HTTP-based Gelf sender. This sender uses Java's HTTP client to {@code POST} JSON Gelf messages to an endpoint.
 *
 * @author Aleksandar Stojadinovic
 * @author Patrick Brueckner
 * @since 1.9
 */
public class GelfHTTPSender implements GelfSender {

    private static final int HTTP_SUCCESSFUL_LOWER_BOUND = 200;
    private static final int HTTP_SUCCESSFUL_UPPER_BOUND = 299;

    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final ErrorReporter errorReporter;
    private final URL url;

    /**
     * Create a new {@link GelfHTTPSender} given {@code url}, {@code connectTimeoutMs}, {@code readTimeoutMs} and
     * {@link ErrorReporter}.
     *
     * @param url target URL
     * @param connectTimeoutMs connection timeout in milliseconds.
     * @param readTimeoutMs read timeout in milliseconds.
     * @param errorReporter the error reporter.
     */
    public GelfHTTPSender(URL url, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter) {

        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.errorReporter = errorReporter;
        this.url = url;
    }

    @Override
    public boolean sendMessage(GelfMessage message) {

        HttpURLConnection connection = null;

        try {

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-type", "application/json");
            if (url.getUserInfo() != null) {
                String basicAuth = "Basic " + Base64.getEncoder().encodeToString(url.getUserInfo().getBytes());
                connection.addRequestProperty("Authorization", basicAuth);
            }

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(message.toJson().getBytes());
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode >= HTTP_SUCCESSFUL_LOWER_BOUND && responseCode <= HTTP_SUCCESSFUL_UPPER_BOUND) {
                return true;
            }

            errorReporter.reportError("Server responded with unexpected status code: " + responseCode, null);

        } catch (IOException e) {
            errorReporter.reportError("Cannot send data to " + url, e);
        } finally {
            // disconnecting HttpURLConnection here to avoid underlying premature underlying Socket being closed.
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    @Override
    public void close() {
    }
}
