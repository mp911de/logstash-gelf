package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
public class GelfREDISSenderIntegrationTests {

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
