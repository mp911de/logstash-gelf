package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * (c) https://github.com/strima/logstash-gelf.git
 */
public class GelfREDISSender implements GelfSender {
    private JedisPool jedisPool;
    private ErrorReporter errorReporter;
    private String redisKey;

    public GelfREDISSender(JedisPool jedisPool, String redisKey, ErrorReporter errorReporter) throws IOException {
        this.jedisPool = jedisPool;
        this.errorReporter = errorReporter;
        this.redisKey = redisKey;
    }

    public boolean sendMessage(GelfMessage message) {
        if (!message.isValid()) {
            return false;
        }

        Jedis jedisClient = null;
        try {
            jedisClient = jedisPool.getResource();
            jedisClient.lpush(redisKey,message.toJson(""));
            return true;
        } catch (Exception e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send REDIS data with key URI " + redisKey, e));
            return false;
        }  finally {
            if(jedisClient != null) {
                jedisPool.returnResource(jedisClient);
            }
        }
    }

    public void close() {
        // We don't need anything -> we use a pool!
    }
}
