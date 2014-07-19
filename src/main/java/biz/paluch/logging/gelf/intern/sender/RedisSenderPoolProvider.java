package biz.paluch.logging.gelf.intern.sender;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton for administration of commonly used jedis pools
 * 
 * @author (c) https://github.com/Batigoal/logstash-gelf.git
 * 
 */
enum RedisSenderPoolProvider {
    INSTANCE;

    private Map<String, JedisPool> pools = new HashMap<String, JedisPool>();

    public synchronized JedisPool getJedisPool(URI hostURI, int configuredPort) {
        return getJedisPool(hostURI, configuredPort, Protocol.DEFAULT_TIMEOUT);
    }

    public synchronized JedisPool getJedisPool(URI hostURI, int configuredPort, int timeoutMs) {
        String lowerCasedConnectionString = hostURI.toString().toLowerCase();
        String cleanConnectionString = lowerCasedConnectionString.substring(0, lowerCasedConnectionString.length()
                - hostURI.getFragment().length());

        if (!pools.containsKey(cleanConnectionString)) {

            String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
            int database = Protocol.DEFAULT_DATABASE;
            if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
            }
            int port = hostURI.getPort() > 0 ? hostURI.getPort() : configuredPort;
            JedisPool newPool = new JedisPool(new JedisPoolConfig(), hostURI.getHost(), port, timeoutMs,                 password, database);

            pools.put(cleanConnectionString, newPool);
        }
        return pools.get(cleanConnectionString);
    }
}
