package biz.paluch.logging.gelf.intern.sender;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

/**
 * Pool holder for {@link Pool} that keeps track of Jedis pools identified by {@link URI}.
 *
 * This implementation synchronizes {@link #getJedisPool(URI, int)} and {@link Pool#destroy()} calls to avoid lingering
 * resources and acquisition of disposed resources. creation
 *
 * @author Mark Paluch
 */
class RedisPoolHolder {

    private final static RedisPoolHolder INSTANCE = new RedisPoolHolder();

    private final Map<String, JedisPoolProxy> standalonePools = new ConcurrentHashMap<>();

    private final Object mutex = new Object();

    public static RedisPoolHolder getInstance() {
        return INSTANCE;
    }

    public Pool<Jedis> getJedisPool(URI hostURI, int configuredPort) {

        synchronized (mutex) {

            String lowerCasedConnectionString = hostURI.toString().toLowerCase();
            final String cleanConnectionString = hostURI.getFragment() != null ? lowerCasedConnectionString.substring(0,
                    lowerCasedConnectionString.length() - hostURI.getFragment().length()) : lowerCasedConnectionString;

            if (standalonePools.containsKey(cleanConnectionString)) {

                JedisPoolProxy poolProxy = standalonePools.get(cleanConnectionString);
                poolProxy.incrementRefCnt();
                return poolProxy;
            }

            Pool<Jedis> jedisPool = JedisPoolFactory.createJedisPool(hostURI, configuredPort, Protocol.DEFAULT_TIMEOUT);

            JedisPoolProxy proxy = new JedisPoolProxy(jedisPool, new Runnable() {

                @Override
                public void run() {
                    standalonePools.remove(cleanConnectionString);
                }

            });

            standalonePools.put(cleanConnectionString, proxy);

            return proxy;
        }
    }

    /**
     * Singleton for administration of commonly used jedis pools
     *
     * @author https://github.com/Batigoal/logstash-gelf.git
     * @author Mark Paluch
     */
    private enum JedisPoolFactory {

        STANDALONE {

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
            public redis.clients.jedis.util.Pool<Jedis> createPool(URI hostURI, int configuredPort, int timeoutMs) {

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }

                int port = hostURI.getPort() > 0 ? hostURI.getPort() : configuredPort;
                return new JedisPool(new JedisPoolConfig(), hostURI.getHost(), port, timeoutMs, password, database);
            }

        },
        SENTINEL

        {

            public static final String MASTER_ID = "masterId";

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
            public redis.clients.jedis.util.Pool<Jedis> createPool(URI hostURI, int configuredPort, int timeoutMs) {

                Set<String> sentinels = getSentinels(hostURI);
                String masterName = getMasterName(hostURI);

                // No logging for Jedis Sentinel at all.
                Logger.getLogger(JedisSentinelPool.class.getName()).setLevel(Level.OFF);

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }

                return new JedisSentinelPool(masterName, sentinels, new JedisPoolConfig(), timeoutMs, password, database);
            }

            protected String getMasterName(URI hostURI) {
                String masterName = "master";

                if (hostURI.getQuery() != null) {
                    String[] keyValues = hostURI.getQuery().split("\\&");
                    for (String keyValue : keyValues) {
                        String[] parts = keyValue.split("\\=");
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
                Set<String> sentinels = new HashSet<>();

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

        abstract redis.clients.jedis.util.Pool<Jedis> createPool(URI hostURI, int configuredPort, int timeoutMs);

        public static redis.clients.jedis.util.Pool<Jedis> createJedisPool(URI hostURI, int configuredPort, int timeoutMs) {

            for (JedisPoolFactory provider : JedisPoolFactory.values()) {
                if (provider.getScheme().equals(hostURI.getScheme())) {
                    return provider.createPool(hostURI, configuredPort, timeoutMs);
                }

            }

            throw new IllegalArgumentException("Scheme " + hostURI.getScheme() + " not supported");
        }

    }

    private class JedisPoolProxy extends Pool<Jedis> {

        private final AtomicLong refCnt = new AtomicLong(1);

        private final Runnable onDestroy;

        private final Pool<Jedis> delegate;

        private JedisPoolProxy(Pool<Jedis> delegate, Runnable onDestroy) {
            this.onDestroy = onDestroy;
            this.delegate = delegate;
        }

        @Override
        public void close() {
            delegate.close();
        }

        public void incrementRefCnt() {
            refCnt.incrementAndGet();
        }

        @Override
        public boolean isClosed() {
            return delegate.isClosed();
        }

        @Override
        public void initPool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<Jedis> factory) {
            delegate.initPool(poolConfig, factory);
        }

        @Override
        public Jedis getResource() {
            return delegate.getResource();
        }

        @Override
        public void returnResourceObject(Jedis resource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void destroy() {

            synchronized (mutex) {

                long val = refCnt.decrementAndGet();

                if (val == 0) {
                    onDestroy.run();
                    delegate.destroy();
                }
            }
        }

        @Override
        public int getNumActive() {
            return delegate.getNumActive();
        }

        @Override
        public int getNumIdle() {
            return delegate.getNumIdle();
        }

        @Override
        public int getNumWaiters() {
            return delegate.getNumWaiters();
        }

        @Override
        public long getMeanBorrowWaitTimeMillis() {
            return delegate.getMeanBorrowWaitTimeMillis();
        }

        @Override
        public long getMaxBorrowWaitTimeMillis() {
            return delegate.getMaxBorrowWaitTimeMillis();
        }

        @Override
        public void addObjects(int count) {
            throw new UnsupportedOperationException();
        }

    }

}
