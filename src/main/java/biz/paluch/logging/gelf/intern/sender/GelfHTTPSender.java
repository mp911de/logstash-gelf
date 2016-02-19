package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static biz.paluch.logging.gelf.GelfUtil.addDefaultPortIfMissing;

/**
 * Created by https://github.com/salex89
 */
public class GelfHTTPSender implements GelfSender {

    final private String uri;
    final private int port;
    final private ErrorReporter errorReporter;
    final private CloseableHttpClient httpClient;


    public GelfHTTPSender(String uri, int port, ErrorReporter errorReporter, HttpClientBuilder builder) {

        this.uri = addDefaultPortIfMissing(uri, String.valueOf(port));
        this.port = port;
        this.errorReporter = errorReporter;


        httpClient = builder.build();
    }

    @Override
    public boolean sendMessage(GelfMessage message) {

        CloseableHttpResponse httpResponse = null;
        try {
            HttpPost post = new HttpPost(uri);
            String messageJson = message.toJson();
            post.setEntity(new StringEntity(messageJson));
            httpResponse = httpClient.execute(post);
            int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
            if (responseStatusCode == 202) {
                return true;
            } else {
                errorReporter.reportError("HTTP responded with non-202 status code: " +
                        responseStatusCode, new IOException("Cannot send data to " + uri + ":" + port));
            }
        } catch (UnsupportedEncodingException e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot create HTTP GELF message to ", e));
        } catch (ClientProtocolException e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send data to " + uri + ":" + port, e));
        } catch (IOException e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send data to " + uri + ":" + port, e));
        } finally {
            if (httpResponse != null)
                Closer.close(httpResponse);
        }
        return false;
    }

    @Override
    public void close() {
        Closer.close(httpClient);
    }
}
