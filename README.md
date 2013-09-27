logstash-gelf-java
=========================
Provides logging to logstash using the Graylog Extended Logging Format (GELF). This implementation comes with support for three areas:

* Java Util Logging
* log4j 1.2.x
* JBoss 7 (mix of Java Util Logging with log4j MDC)


Settings
--------------
Following settings can be used:
* graylogHost (Mandatory): Hostname/IP-Address of the Graylog Host
    * tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com
    * udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com
    * (the host) for UDP, e.g. 127.0.0.1 or some.host.com
* graylogPort (Optional): Port, default 12201
* originHost (Optional): Originating Hostname, default FQDN Hostname
* extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false
* filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false
* mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See MDC Profiling, default false
* facility (Optional): Name of the Facility, default gelf-java
* threshold/level (Optional): Log-Level, default INFO
* filter (Optional): Class-Name of a Log-Filter, default none
* additionalFields (Optional): Post additional fields. Eg. .GelfLogHandler.additionalFields=fieldName=Value
* mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated mdcFields=Application,Version,SomeOtherFieldName

MDC Profiling
--------------
MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. Only available in log4j environments.
You must set one value in the MDC:


profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)
Two values are set by the Log Appender:

profiling.requestEnd: End-Time of the Request-End in Date.toString-representation
profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)


Including it in your project
--------------

Maven

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <version>1.0.0-SNAPSHOT</version>
        <artifactId>logstash-gelf-java</artifactId>
    </dependency>


Java Util Logging Configuration
--------------
Properties:

    handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler

    .handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler
    .level = INFO

    biz.paluch.logging.gelf.jul.GelfLogHandler.graylogHost=udp:localhost
    biz.paluch.logging.gelf.jul.GelfLogHandler.graylogPort=12201
    biz.paluch.logging.gelf.jul.GelfLogHandler.facility=java-test
    biz.paluch.logging.gelf.jul.GelfLogHandler.extractStackTrace=true
    biz.paluch.logging.gelf.jul.GelfLogHandler.filterStackTrace=true
    biz.paluch.logging.gelf.jul.GelfLogHandler.timestampPattern=yyyy-MM-dd HH:mm:ss,SSSS
    biz.paluch.logging.gelf.jul.GelfLogHandler.maximumMessageSize=8192
    biz.paluch.logging.gelf.jul.GelfLogHandler.additionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
    biz.paluch.logging.gelf.jul.GelfLogHandler.level=INFO


log4j Configuration
--------------
Properties:

    log4j.appender.gelf=biz.paluch.logging.gelf.log4j.GelfLogAppender
    log4j.appender.gelf.Threshold=INFO
    log4j.appender.gelf.GraylogHost=udp:localhost
    log4j.appender.gelf.GraylogPort=12201
    log4j.appender.gelf.Facility=java-test
    log4j.appender.gelf.ExtractStackTrace=true
    log4j.appender.gelf.FilterStackTrace=true
    log4j.appender.gelf.MdcProfiling=true
    log4j.appender.gelf.TimestampPattern=yyyy-MM-dd HH:mm:ss,SSSS
    log4j.appender.gelf.MaximumMessageSize=8192
    log4j.appender.gelf.AdditionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
    log4j.appender.gelf.MdcFields=mdcField1,mdcField2


XML:

    <appender name="gelf" class="biz.paluch.logging.gelf.log4j.GelfLogAppender">
        <param name="Threshold" value="INFO" />
        <param name="GraylogHost" value="udp:localhost" />
        <param name="GraylogPort" value="12201" />
        <param name="Facility" value="java-test" />
        <param name="ExtractStackTrace" value="true" />
        <param name="FilterStackTrace" value="true" />
        <param name="MdcProfiling" value="true" />
        <param name="TimestampPattern" value="yyyy-MM-dd HH:mm:ss,SSSS" />
        <param name="MaximumMessageSize" value="8192" />
        <param name="AdditionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
        <param name="MdcFields" value="mdcField1,mdcField2" />
    </appender>


JBoss 7 Configuration
--------------
You need to include the library as module, then add following lines to your configuration:

standalone.xml

    <custom-handler name="GelfLogger" class="biz.paluch.logging.gelf.jboss7.JBoss7GelfLogHandler" module="biz.paluch.logging">
        <level name="INFO" />
        <properties>
            <property name="threshold" value="INFO" />
            <property name="graylogHost" value="udp:localhost" />
            <property name="graylogPort" value="12201" />
            <property name="facility" value="java-test" />
            <property name="extractStackTrace" value="true" />
            <property name="filterStackTrace" value="true" />
            <property name="mdcProfiling" value="true" />
            <property name="timestampPattern" value="yyyy-MM-dd HH:mm:ss,SSSS" />
            <property name="maximumMessageSize" value="8192" />
            <property name="additionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
            <property name="mdcFields" value="mdcField1,mdcField2" />
        </properties>
    </custom-handler>



License
-------
* [The MIT License (MIT)] (http://opensource.org/licenses/MIT)
* Contains also code from https://github.com/t0xa/gelfj