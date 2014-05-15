package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * 
 * (c) https://github.com/Batigoal/logstash-gelf.git
 *
 */
public class RedisGelfSenderProvider implements GelfSenderProvider {
    
    private static final JedisPoolConfig configuration = new JedisPoolConfig();
    
    @Override
    public boolean supports(String host) {
        return host.startsWith("redis");
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        String graylogHost = configuration.getHost();

        URI hostUri = URI.create(graylogHost);
        JedisPool pool = RedisSenderPoolProvider.INSTANCE.getJedisPool(hostUri, configuration.getPort());
        return new GelfREDISSender(pool, hostUri.getFragment(), configuration.getErrorReport());
        
    }
    
    public JedisPoolConfig getConfiguration() {
        return configuration;
    }

   /**
    * Singleton for administration of commonly used jedis pools 
    * 
    * @author (c) https://github.com/Batigoal/logstash-gelf.git
    *
    */
    private static enum RedisSenderPoolProvider {
        INSTANCE;
        
        private Map<String, JedisPool> pools = new HashMap<String, JedisPool>();

        public synchronized JedisPool getJedisPool(URI hostURI, int configuredPort) {
            String lowerCasedHost = hostURI.getHost().toLowerCase();

            if (!pools.containsKey(lowerCasedHost)) {

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if(hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }
                int port = hostURI.getPort() > 0 ? hostURI.getPort() : configuredPort;
                JedisPool newPool = new JedisPool(
                        configuration, 
                        hostURI.getHost(), 
                        port,
                        Protocol.DEFAULT_TIMEOUT, 
                        password, 
                        database);
                
                pools.put(lowerCasedHost, newPool);
            }
            return pools.get(lowerCasedHost);
        }
    }

}
