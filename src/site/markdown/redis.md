logstash-gelf over Redis
--------------
logstash-gelf can be used together with Redis for shipping log events. Redis Standalone and Redis Sentinel can be used. logstash-gelf uses jedis (https://github.com/xetorthio/jedis)
as Redis Client. logstash-gelf uses Redis Lists (LPUSH) for storing. channel/pattern_channel (SUBSCRIBE/PSUBSCRIBE) are not supported.

## Redis Standalone
 The URI used as connection property is a java.net.URI, therefore it can have all components. The minimal URI must contain at least a host and
 the Fragment (List-Name).

    redis://[:password@]host[:port]/[databaseNo]#Listname

Example:

    redis://localhost#logstash
    redis://localhost/1#logstash
    redis://:password@localhost:6379/1#logstash

   * scheme    (fixed: redis, directly used to determine the to be used sender class)
   * user-info (variable: only the password part is used since redis doesn't have users, indirectly used from jedis)
   * host      (variable: the host your redis db runs on, indirectly used from jedis)
   * port      (variable: the port your redis db runs on, indirectly used from jedis)
   * path      (variable: your redis db number for Redis, indirectly used from jedis)
   * fragment  (variable: the listname we push the log messages via LPUSH, directly used)


## Redis Sentinel

    redis-sentinel://[:password@]host[:port][,host[:port]]/[databaseNo][?masterId=sentinelMasterId]#Listname

Example:

    redis-sentinel://localhost/1#logstash
    redis-sentinel://:password@localhost:26379,otherhost:26379/1?masterId=mymaster#logstash

   * scheme    (fixed: redis-sentinel, directly used to determine the to be used sender class)
   * user-info (variable: only the password part is used since redis doesn't have users, indirectly used from jedis)
   * host      (variable: the host your redis db runs on, indirectly used from jedis)
   * port      (variable: the port your redis db runs on, indirectly used from jedis)
   * path      (variable: your redis db number for Redis, indirectly used from jedis)
   * fragment  (variable: the listname we push the log messages via LPUSH, directly used)
   * query string
      * masterId (variable: the sentinel master Id)
