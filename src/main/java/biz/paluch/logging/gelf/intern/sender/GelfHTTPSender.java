package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by https://github.com/salex89
 */
public class GelfHTTPSender implements GelfSender {

    final private ErrorReporter errorReporter;
    final private URL url;
    private HttpURLConnection connection = null;
    private int HTTP_ACCEPTED_STATUS = 202;

    public GelfHTTPSender(URL url, ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        this.url = url;
    }

    @Override public boolean sendMessage(GelfMessage message) {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(message.toJson().getBytes());
            outputStream.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_ACCEPTED_STATUS) {
                return true;
            } else {
                errorReporter.reportError("Server responded with unexpected status code: ", null);
            }
        } catch (IOException e) {
            errorReporter.reportError("Error when sending log message", e);
        }
        return false;
    }

    @Override public void close() {
        //disconnecting HttpURLConnection here to avoid underlying premature underlying Socket being closed.
        if (connection != null)
            connection.disconnect();
    }
}
