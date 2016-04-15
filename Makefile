PATH := ./work/redis-git/src:${PATH}

define REDIS1_CONF
daemonize yes
port 6479
pidfile work/redis1-6479.pid
logfile work/redis1-6479.log
save ""
appendonly no
client-output-buffer-limit pubsub 256k 128k 5
endef

# SENTINELS
define REDIS_SENTINEL1
port 26379
daemonize yes
sentinel monitor mymaster 127.0.0.1 6479 1
sentinel down-after-milliseconds mymaster 2000
sentinel failover-timeout mymaster 120000
sentinel parallel-syncs mymaster 1
pidfile work/sentinel1-26379.pid
logfile work/sentinel1-26379.log
endef

export REDIS1_CONF
export REDIS_SENTINEL1


start: cleanup
	echo "$$REDIS1_CONF" > work/redis1-6479.conf && redis-server work/redis1-6479.conf
	echo "$$REDIS_SENTINEL1" > work/sentinel1-26379.conf && redis-server work/sentinel1-26379.conf --sentinel


cleanup: stop
	- mkdir -p work
	rm -f work/dump.rdb work/appendonly.aof work/*.conf work/*.log 2>/dev/null

stop:
	pkill redis-server || true
	pkill redis-sentinel || true
	sleep 2
	rm -f work/dump.rdb work/appendonly.aof work/*.conf work/*.log || true
	rm -f *.aof
	rm -f *.rdb


test-coveralls:
	make start
	sleep 2
	mvn -B -Dtest.withRedis=true clean compile cobertura:cobertura coveralls:report
	make stop

test:
	make start
	sleep 2
	mvn -B -Dtest.withRedis=true clean verify
	make stop

travis-install:
	pkill redis-server || true
	pkill redis-sentinel || true
	[ ! -e work/redis-git ] && git clone https://github.com/antirez/redis.git --branch 2.8 work/redis-git && cd work/redis-git|| true
	[ -e work/redis-git ] && cd work/redis-git && git reset --hard && git pull && git checkout 2.8 || true
	make -C work/redis-git clean
	make -C work/redis-git -j4

release:
	mvn release:clean
	mvn release:prepare -Psonatype-oss-release
	mvn release:perform -Psonatype-oss-release
	mvn site:site
	mvn -o scm-publish:publish-scm -Dgithub.site.upload.skip=false

.PHONY: test

