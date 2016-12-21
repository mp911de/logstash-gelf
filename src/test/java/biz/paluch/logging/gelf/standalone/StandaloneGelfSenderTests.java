package biz.paluch.logging.gelf.standalone;

import static biz.paluch.logging.gelf.GelfMessageBuilder.newInstance;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;

/**
 * @author Mark Paluch
 */
public class StandaloneGelfSenderTests {

    @Before
    public void before() throws Exception {
        GelfTestSender.getMessages().clear();
    }

    @Test
    public void testStandalone() throws Exception {
        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration(new Slf4jErrorReporter());

        configuration.setHost("test:standalone");
        configuration.setPort(12345);

        GelfSender sender = GelfSenderFactory.createSender(configuration);

        sender.sendMessage(newInstance().withFullMessage("message").withFacility(null).build());

        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertThat(gelfMessage.getFullMessage()).isEqualTo("message");
        assertThat(gelfMessage.toJson()).isEqualTo("{\"full_message\":\"message\"}");

    }
}
