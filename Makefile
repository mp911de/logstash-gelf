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

export REDIS1_CONF

start: cleanup
	echo "$$REDIS1_CONF" > work/redis1-6479.conf && redis-server work/redis1-6479.conf

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
	mvn -B -Dtest.withRedis=true clean compile cobertura:cobertura coveralls:cobertura
	make stop

test:
	make start
	sleep 2
	mvn -B -Dtest.withRedis=true clean compile test
	make stop

travis-install:
	pkill redis-server || true
	pkill redis-sentinel || true
	[ ! -e work/redis-git ] && git clone https://github.com/antirez/redis.git --branch 3.0 work/redis-git && cd work/redis-git|| true
	[ -e work/redis-git ] && cd work/redis-git && git reset --hard && git pull && git checkout 3.0 || true
	make -C work/redis-git clean
	make -C work/redis-git -j4

.PHONY: test

