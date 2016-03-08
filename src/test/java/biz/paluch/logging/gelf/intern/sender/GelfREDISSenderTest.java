package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.GelfMessage;
import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class GelfREDISSenderTest {

    @Test
    public void recursiveCallIsBlocked() throws Exception {
        TestGefRedisSender sut = new TestGefRedisSender();
        sut.sendMessage(new GelfMessage());

        Field field = GelfREDISSender.class.getDeclaredField("callers");
        field.setAccessible(true);
        Set<?> callers = (Set<?>) field.get(sut);
        assertThat(callers).isEmpty();
    }

    static class TestGefRedisSender extends GelfREDISSender {

        public TestGefRedisSender() throws IOException {
            super(null, null, null);
        }

        @Override
        protected boolean sendMessage0(GelfMessage message) {

            // recursive call
            return super.sendMessage(message);
        }
    }
}