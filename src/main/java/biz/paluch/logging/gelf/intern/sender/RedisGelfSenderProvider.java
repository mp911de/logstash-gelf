package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.net.URI;

/**
 * 
 * (c) https://github.com/Batigoal/logstash-gelf.git
 * 
 */
public class RedisGelfSenderProvider implements GelfSenderProvider {

    @Override
    public boolean supports(String host) {
        return host.startsWith("redis:");
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        String graylogHost = configuration.getHost();

        URI hostUri = URI.create(graylogHost);
        int port = hostUri.getPort();
        if (port == 0) {
            port = configuration.getPort();
        }

        if (port == 0) {
            port = Protocol.DEFAULT_PORT;
        }

        JedisPool pool = RedisSenderPoolProvider.INSTANCE.getJedisPool(hostUri, port);
        return new GelfREDISSender(pool, hostUri.getFragment(), configuration.getErrorReporter());

    }
}
