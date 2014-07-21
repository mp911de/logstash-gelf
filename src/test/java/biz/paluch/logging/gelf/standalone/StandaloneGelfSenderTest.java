package biz.paluch.logging.gelf.standalone;

import static biz.paluch.logging.gelf.GelfMessageBuilder.newInstance;
import static org.junit.Assert.assertEquals;
import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import org.junit.Before;
import org.junit.Test;

public class StandaloneGelfSenderTest {

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

        assertEquals(1, GelfTestSender.getMessages().size());

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        assertEquals("message", gelfMessage.getFullMessage());
        assertEquals("{\"full_message\":\"message\"}", gelfMessage.toJson());

    }
}
