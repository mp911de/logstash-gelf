package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.io.IOException;

/**
 * (c) https://github.com/strima/logstash-gelf.git
 */
public class GelfREDISSender<T> implements GelfSender {
    private Pool<Jedis> jedisPool;
    private ErrorReporter errorReporter;
    private String redisKey;

    public GelfREDISSender(Pool<Jedis> jedisPool, String redisKey, ErrorReporter errorReporter) throws IOException {
        this.jedisPool = jedisPool;
        this.errorReporter = errorReporter;
        this.redisKey = redisKey;
    }

    public boolean sendMessage(GelfMessage message) {

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
                jedisPool.returnResource(jedisClient);
            }
        }
    }

    public void close() {
        // We don't need anything -> we use a pool!
    }
}
