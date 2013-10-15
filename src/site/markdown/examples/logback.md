Settings
--------------
Following settings can be used:

 * host (Mandatory): Hostname/IP-Address of the Logstasg Host
     * tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com
     * udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com
     * (the host) for UDP, e.g. 127.0.0.1 or some.host.com
 * port (Optional): Port, default 12201
 * originHost (Optional): Originating Hostname, default FQDN Hostname
 * extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false
 * filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false
 * mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See MDC Profiling, default false. See [MDC Profiling](../mdcprofiling.html) for details.
 * facility (Optional): Name of the Facility, default logstash-gelf
 * threshold/level (Optional): Log-Level, default INFO
 * filter (Optional): Class-Name of a Log-Filter, default none
 * additionalFields (Optional): Post additional fields. Eg. .GelfLogHandler.additionalFields=fieldName=Value
 * mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated mdcFields=Application,Version,SomeOtherFieldName


Logback configuration
--------------

logback.xml Example:

    <?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE configuration>

    <configuration>
        <contextName>test</contextName>
        <jmxConfigurator/>

        <appender name="gelf" class="biz.paluch.logging.gelf.logback.GelfLogbackAppender">
            <host>udp:localhost</host>
            <port>12201</port>
            <facility>java-test</facility>
            <extractStackTrace>true</extractStackTrace>
            <filterStackTrace>true</filterStackTrace>
            <mdcProfiling>true</mdcProfiling>
            <timestampPattern>yyyy-MM-dd HH:mm:ss,SSSS</timestampPattern>
            <maximumMessageSize>8192</maximumMessageSize>
            <additionalFields>fieldName1=fieldValue1,fieldName2=fieldValue2</additionalFields>
            <mdcFields>mdcField1,mdcField2</mdcFields>
            <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
                <level>INFO</level>
            </filter>
        </appender>

        <root level="DEBUG">
            <appender-ref ref="gelf" />
        </root>
    </configuration>
        