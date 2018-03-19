package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import com.sun.xml.internal.messaging.saaj.util.Base64;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
     * @param url
     * @param connectTimeoutMs
     * @param readTimeoutMs
     * @param errorReporter
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
            if (url.getUserInfo() != null) {
                String basicAuth = "Basic " + new String(Base64.encode(url.getUserInfo().getBytes()));
                connection.setRequestProperty("Authorization", basicAuth);
            }
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-type", "application/json");

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
