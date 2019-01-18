package biz.paluch.logging.gelf.intern.sender;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

/**
 * Singleton for administration of commonly used jedis pools
 *
 * @author https://github.com/Batigoal/logstash-gelf.git
 * @author Mark Paluch
 */
enum RedisSenderPoolProvider {

    STANDALONE {
        private Map<String, JedisPool> standalonePools = new HashMap<String, JedisPool>();

        @Override
        public String getScheme() {
            return RedisSenderConstants.REDIS_SCHEME;
        }

        /**
         * Create a Jedis Pool for standalone Redis Operations.
         *
         * @param hostURI
         * @param configuredPort
         * @param timeoutMs
         * @return Pool<Jedis>
         */
        @Override
        public synchronized Pool<Jedis> getJedisPool(URI hostURI, int configuredPort, int timeoutMs) {
            String lowerCasedConnectionString = hostURI.toString().toLowerCase();
            String cleanConnectionString = lowerCasedConnectionString.substring(0, lowerCasedConnectionString.length()
                    - hostURI.getFragment().length());

            if (!standalonePools.containsKey(cleanConnectionString)) {

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }
                int port = hostURI.getPort() > 0 ? hostURI.getPort() : configuredPort;
                JedisPool newPool = new JedisPool(new JedisPoolConfig(), hostURI.getHost(), port, timeoutMs, password, database);

                standalonePools.put(cleanConnectionString, newPool);
            }
            return standalonePools.get(cleanConnectionString);
        }
    },
    SENTINEL {
        public static final String MASTER_ID = "masterId";

        private Map<String, JedisSentinelPool> sentinelPools = new HashMap<String, JedisSentinelPool>();

        @Override
        public String getScheme() {
            return RedisSenderConstants.REDIS_SENTINEL_SCHEME;
        }

        /**
         * Create a Jedis Pool for sentinel Redis Operations.
         *
         * @param hostURI
         * @param configuredPort
         * @param timeoutMs
         * @return Pool<Jedis>
         */
        @Override
        public Pool<Jedis> getJedisPool(URI hostURI, int configuredPort, int timeoutMs) {
            String lowerCasedConnectionString = hostURI.toString().toLowerCase();
            String cleanConnectionString = lowerCasedConnectionString.substring(0, lowerCasedConnectionString.length()
                    - hostURI.getFragment().length());

            Set<String> sentinels = getSentinels(hostURI);
            String masterName = getMasterName(hostURI);

            // No logging for Jedis Sentinel at all.
            Logger.getLogger(JedisSentinelPool.class.getName()).setLevel(Level.OFF);

            if (!sentinelPools.containsKey(cleanConnectionString)) {

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }
                JedisSentinelPool newPool = new JedisSentinelPool(masterName, sentinels, new JedisPoolConfig(), timeoutMs,
                        password, database);

                sentinelPools.put(cleanConnectionString, newPool);
            }
            return sentinelPools.get(cleanConnectionString);
        }

        protected String getMasterName(URI hostURI) {
            String masterName = "master";

            if (hostURI.getQuery() != null) {
                String[] keyValues = hostURI.getQuery().split("\\&");
                for (String keyValue : keyValues) {
                    String parts[] = keyValue.split("\\=");
                    if (parts.length != 2) {
                        continue;
                    }

                    if (parts[0].equals(MASTER_ID)) {
                        masterName = parts[1].trim();
                    }
                }
            }
            return masterName;
        }

        protected Set<String> getSentinels(URI hostURI) {
            Set<String> sentinels = new HashSet<String>();

            String[] sentinelHostNames = hostURI.getHost().split("\\,");
            for (String sentinelHostName : sentinelHostNames) {
                if (sentinelHostName.contains(":")) {
                    sentinels.add(sentinelHostName);
                } else if (hostURI.getPort() > 0) {
                    sentinels.add(sentinelHostName + ":" + hostURI.getPort());
                }
            }
            return sentinels;
        }
    };

    public abstract String getScheme();

    public abstract Pool<Jedis> getJedisPool(URI hostURI, int configuredPort, int timeoutMs);

    public static Pool<Jedis> getJedisPool(URI hostURI, int configuredPort) {

        for (RedisSenderPoolProvider provider : values()) {
            if (provider.getScheme().equals(hostURI.getScheme())) {
                return provider.getJedisPool(hostURI, configuredPort, Protocol.DEFAULT_TIMEOUT);
            }

        }

        throw new IllegalArgumentException("Scheme " + hostURI.getScheme() + " not supported");
    }

}
