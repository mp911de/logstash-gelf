logback
=========

Following settings can be used:

| Attribute Name    | Description                          | Default |
| ----------------- |:------------------------------------:|:-------:|
| host              | Hostname/IP-Address of the Logstash host. The `host` field accepts following forms: <ul><li>`tcp:hostname` for TCP transport, e. g. `tcp:127.0.0.1` or `tcp:some.host.com` </li><li>`udp:hostname` for UDP transport, e. g. `udp:127.0.0.1`, `udp:some.host.com` or just `some.host.com`  </li><li>`redis://[:password@]hostname:port/db-number#listname` for Redis transport. See [Redis transport for logstash-gelf](../redis.html) for details. </li><li>`redis-sentinel://[:password@]hostname:port/db-number?masterId=masterId#listname` for Redis transport with Sentinel lookup. See [Redis transport for logstash-gelf](../redis.html) for details. </li></ul> | none | 
| port              | Port of the Logstash host  | `12201` |
| version           | GELF Version `1.0` or `1.1` | `1.0` |
| originHost        | Originating Hostname  | FQDN Hostname |
| extractStackTrace | Send the Stack-Trace to the StackTrace field (`true`/`false`)  | `false` |
| filterStackTrace  | Perform Stack-Trace filtering (`true`/`false`)| `false` |
| facility          | Name of the Facility  | `logstash-gelf` |
| mdcProfiling      | Perform Profiling (Call-Duration) based on MDC Data. See [MDC Profiling](../mdcprofiling.html) for details  | `false` |
| additionalFields  | Send additional static fields. The fields are specified as key-value pairs are comma-separated. Example: `additionalFields=fieldName=Value,fieldName2=Value2` | none |
| mdcFields         | Send additional fields whose values are obtained from MDC. Name of the Fields are comma-separated. Example: `mdcFields=Application,Version,SomeOtherFieldName` | none |
| dynamicMdcFields  | Dynamic MDC Fields allows you to extract MDC values based on one or more regular expressions. Multiple regexes are comma-separated. The name of the MDC entry is used as GELF field name. | none |
| includeFullMdc    | Include all fields from the MDC. | `false` |
| maximumMessageSize| Maximum message size (in bytes). If the message size is exceeded, the appender will submit the message in multiple chunks. | `8192` |
| timestampPattern  | Date/time pattern for the `Time` field| `yyyy-MM-dd HH:mm:ss,SSSS` |


The only mandatory field is `host`. All other fields are optional.


Logback Configuration
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
            <version>1.0</version>
            <facility>java-test</facility>
            <extractStackTrace>true</extractStackTrace>
            <filterStackTrace>true</filterStackTrace>
            <mdcProfiling>true</mdcProfiling>
            <timestampPattern>yyyy-MM-dd HH:mm:ss,SSSS</timestampPattern>
            <maximumMessageSize>8192</maximumMessageSize>
            <additionalFields>fieldName1=fieldValue1,fieldName2=fieldValue2</additionalFields>
            <mdcFields>mdcField1,mdcField2</mdcFields>
            <dynamicMdcFields>myMdc.*,[a-z]+Field</dynamicMdcFields>
            <includeFullMdc>true</includeFullMdc>
            <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
                <level>INFO</level>
            </filter>
        </appender>

        <root level="DEBUG">
            <appender-ref ref="gelf" />
        </root>
    </configuration>
        