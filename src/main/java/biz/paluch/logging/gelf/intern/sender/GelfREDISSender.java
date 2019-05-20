package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * @author https://github.com/strima/logstash-gelf.git
 * @author Mark Paluch
 * @since 1.5
 */
public class GelfREDISSender<T> implements GelfSender {

    private final Pool<Jedis> jedisPool;
    private final ErrorReporter errorReporter;
    private final String redisKey;
    private final Set<Thread> callers = Collections.newSetFromMap(new WeakHashMap<Thread, Boolean>());

    public GelfREDISSender(Pool<Jedis> jedisPool, String redisKey, ErrorReporter errorReporter) {
        this.jedisPool = jedisPool;
        this.errorReporter = errorReporter;
        this.redisKey = redisKey;
    }

    public boolean sendMessage(GelfMessage message) {

        // prevent recursive self calls caused by the Redis driver since it
        if (!callers.add(Thread.currentThread())) {
            return false;
        }

        try {
            return sendMessage0(message);
        } finally {
            callers.remove(Thread.currentThread());
        }
    }

    protected boolean sendMessage0(GelfMessage message) {
        Jedis jedisClient = null;
        try {
            jedisClient = jedisPool.getResource();
            jedisClient.lpush(redisKey, message.toJson(""));
            return true;
        } catch (Exception e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send REDIS data with key URI " + redisKey, e));
            return false;
        } finally {
            if (jedisClient != null) {
                jedisClient.close();
            }
        }
    }

    public void close() {
        callers.clear();
        jedisPool.destroy();
    }
}
