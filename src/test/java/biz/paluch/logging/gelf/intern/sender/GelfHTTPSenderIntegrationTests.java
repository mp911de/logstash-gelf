package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.netty.NettyLocalHTTPServer;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Aleksandar Stojadinovic
 * @author Patrick Brueckner
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class GelfHTTPSenderIntegrationTests {

    private static final GelfMessage GELF_MESSAGE = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");
    private NettyLocalHTTPServer server;
    private GelfHTTPSender sender;

    @Mock
    ErrorReporter errorReporter;

    @BeforeEach
    public void setUp() throws Exception {
        server = new NettyLocalHTTPServer();
        server.run();

        sender = new GelfHTTPSender(new URL("http://127.0.0.1:19393"), 1000, 1000, new ErrorReporter() {

            @Override
            public void reportError(String message, Exception e) {

                System.out.println(message);

                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
    }

    @AfterEach
    public void tearDown() {
        server.close();
        sender.close();
    }

    @Test
    public void sendMessageTestWithAcceptedResponse() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        boolean success = sender.sendMessage(gelfMessage);

        assertThat(success).isTrue();
        verifyZeroInteractions(errorReporter);

        List<Object> jsonValues = server.getJsonValues();
        assertThat(jsonValues).hasSize(1);

        Map<String, Object> messageJson = (Map<String, Object>) jsonValues.get(0);
        assertThat(messageJson.get("short_message")).isEqualTo(gelfMessage.getShortMessage());
        assertThat(messageJson.get("full_message")).isEqualTo(gelfMessage.getFullMessage());
        assertThat(messageJson.get("timestamp")).isEqualTo(gelfMessage.getTimestamp());
        assertThat(messageJson.get("level")).isEqualTo(gelfMessage.getLevel());
    }

    @Test
    public void sendMessageTestWithCreatedResponse() throws IOException {

        server.setReturnStatus(HttpResponseStatus.CREATED);

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        boolean success = sender.sendMessage(gelfMessage);

        assertThat(success).isTrue();
        verifyZeroInteractions(errorReporter);

        List<Object> jsonValues = server.getJsonValues();
        assertThat(jsonValues).hasSize(1);

        Map<String, Object> messageJson = (Map<String, Object>) jsonValues.get(0);
        assertThat(messageJson.get("short_message")).isEqualTo(gelfMessage.getShortMessage());
        assertThat(messageJson.get("full_message")).isEqualTo(gelfMessage.getFullMessage());
        assertThat(messageJson.get("timestamp")).isEqualTo(gelfMessage.getTimestamp());
        assertThat(messageJson.get("level")).isEqualTo(gelfMessage.getLevel());
    }

    @Test
    public void shouldUsePostHttpMethod() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        assertThat(success).isTrue();
        assertThat(server.getLastHttpRequest().name()).isEqualTo("POST");
    }

    @Test
    public void shouldUseJsonContentType() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        assertThat(success).isTrue();
        assertThat(server.getLastHttpHeaders().get("Content-type")).isEqualTo("application/json");
    }

    @Test
    public void sendMessageFailureTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

        String uri = "http://127.0.0.1:19393";
        GelfHTTPSender sender = new GelfHTTPSender(new URL(uri), 1000, 1000, errorReporter);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        assertThat(success).isFalse();
        verify(errorReporter, times(1)).reportError(anyString(), ArgumentMatchers.<Exception> isNull());
    }

}
