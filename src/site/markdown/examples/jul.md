Settings
--------------
Following settings can be used:

 * host (since version 1.2.0, Mandatory): Hostname/IP-Address of the Logstash Host
    * tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com
    * udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com
    * redis://\[:REDISDB_PASSWORD@\]REDISDB_HOST:REDISDB_PORT/REDISDB_NUMBER#REDISDB_LISTNAME , e.g. redis://:donttrustme@127.0.0.1:6379/0#myloglist or if no password needed redis://127.0.0.1:6379/0#myloglist
    * (the host) for UDP, e.g. 127.0.0.1 or some.host.com
 * port (since version 1.2.0, Optional): Port, default 12201
 * version (Optional): GELF Version 1.0 or 1.1, default 1.0
 * graylogHost (until version 1.1.0, Mandatory): Hostname/IP-Address of the Logstash Host
 * graylogPort (until version 1.1.0, Optional): Port, default 12201
 * originHost (Optional): Originating Hostname, default FQDN Hostname
 * extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false
 * filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false
 * facility (Optional): Name of the Facility, default logstash-gelf
 * threshold/level (Optional): Log-Level, default INFO
 * filter (Optional): Class-Name of a Log-Filter, default none
 * additionalFields (Optional): Post additional fields. Eg. .GelfLogHandler.additionalFields=fieldName=Value


Please note: The logging Jar files need to be on the boot class path, else JUL won't load the handler. 


Java Util Logging configuration
--------------

Simple Configuration:

    handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler

    .handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler
    .level = INFO

    biz.paluch.logging.gelf.jul.GelfLogHandler.host=udp:localhost
    biz.paluch.logging.gelf.jul.GelfLogHandler.port=12201
    biz.paluch.logging.gelf.jul.GelfLogHandler.level=INFO

Extended Properties:

    handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler

    .handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler
    .level = INFO

    biz.paluch.logging.gelf.jul.GelfLogHandler.host=udp:localhost
    biz.paluch.logging.gelf.jul.GelfLogHandler.port=12201
    biz.paluch.logging.gelf.jul.GelfLogHandler.version=1.0
    biz.paluch.logging.gelf.jul.GelfLogHandler.facility=java-test
    biz.paluch.logging.gelf.jul.GelfLogHandler.extractStackTrace=true
    biz.paluch.logging.gelf.jul.GelfLogHandler.filterStackTrace=true
    biz.paluch.logging.gelf.jul.GelfLogHandler.timestampPattern=yyyy-MM-dd HH:mm:ss,SSSS
    biz.paluch.logging.gelf.jul.GelfLogHandler.maximumMessageSize=8192
    biz.paluch.logging.gelf.jul.GelfLogHandler.additionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
    biz.paluch.logging.gelf.jul.GelfLogHandler.level=INFO
    