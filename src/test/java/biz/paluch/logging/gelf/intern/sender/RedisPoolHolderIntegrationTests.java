package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;
import biz.paluch.logging.gelf.Sockets;

/**
 * Integration tests for {@link RedisPoolHolder}.
 *
 * @author Mark Paluch
 */
class RedisPoolHolderIntegrationTests {

    @BeforeEach
    void setUp() {
        // enable the test with -Dtest.withRedis=true
        assumeTrue(Sockets.isOpen("localhost", 6479));
    }

    @Test
    void shouldConnectToJedis() {

        Pool<Jedis> pool = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), 6479);

        Jedis resource = pool.getResource();
        assertThat(resource.ping()).isEqualTo("PONG");
        resource.close();

        pool.destroy();
        assertThat(pool.isClosed()).isTrue();
    }

    @Test
    void shouldConsiderRefCntBeforeClosingPool() {

        Pool<Jedis> pool1 = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), 6479);
        Pool<Jedis> pool2 = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), 6479);

        assertThat(pool1.isClosed()).isFalse();
        assertThat(pool2.isClosed()).isFalse();

        pool1.destroy();

        assertThat(pool1.isClosed()).isFalse();
        assertThat(pool2.isClosed()).isFalse();

        pool2.destroy();

        assertThat(pool1.isClosed()).isTrue();
        assertThat(pool2.isClosed()).isTrue();
    }

    @Test
    void shouldReturnActivePoolsOnly() {

        RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), 6479).destroy();
        Pool<Jedis> pool = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), 6479);

        assertThat(pool.isClosed()).isFalse();

        pool.destroy();

        assertThat(pool.isClosed()).isTrue();
    }

}
