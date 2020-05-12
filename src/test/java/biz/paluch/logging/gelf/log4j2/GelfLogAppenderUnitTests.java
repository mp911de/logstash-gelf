package biz.paluch.logging.gelf.log4j2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * Unit tests for {@link GelfLogAppender}.
 * 
 * @author Mark Paluch
 */
class GelfLogAppenderUnitTests {

    @Test
    void testStop() {

        GelfSender sender = mock(GelfSender.class);

        GelfLogAppender sut = new GelfLogAppender("name", null, new MdcGelfMessageAssembler(), true);
        sut.gelfSender = sender;

        sut.stop();

        verify(sender).close();
    }

    @Test
    void testStopWithTimeout() {

        GelfSender sender = mock(GelfSender.class);

        GelfLogAppender sut = new GelfLogAppender("name", null, new MdcGelfMessageAssembler(), true);
        sut.gelfSender = sender;

        sut.stop(0, TimeUnit.SECONDS);

        verify(sender).close();
    }
}
