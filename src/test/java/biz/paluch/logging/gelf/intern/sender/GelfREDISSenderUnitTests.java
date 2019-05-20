package biz.paluch.logging.gelf.intern.sender;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;
import biz.paluch.logging.gelf.intern.ErrorReporter;

/**
 * Unit tests for {@link GelfREDISSender}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfREDISSenderUnitTests {

    @Mock
    Pool<Jedis> pool;

    @Mock
    ErrorReporter errorReporter;

    @Test
    void shouldClosePool() {

        GelfREDISSender sender = new GelfREDISSender(pool, "key", errorReporter);

        sender.close();

        verify(pool).destroy();
    }
}
