log4j 1.2.x
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
| threshold         | Log-Level threshold | `INFO` |
| filter            | Class-Name of a Log-Filter  | none |
| additionalFields  | Send additional static fields. The fields are specified as key-value pairs are comma-separated. Example: `additionalFields=fieldName=Value,fieldName2=Value2` | none |
| mdcFields         | Send additional fields whose values are obtained from MDC. Name of the Fields are comma-separated. Example: `mdcFields=Application,Version,SomeOtherFieldName` | none |
| dynamicMdcFields  | Dynamic MDC Fields allows you to extract MDC values based on one or more regular expressions. Multiple regexes are comma-separated. The name of the MDC entry is used as GELF field name. | none |
| includeFullMdc    | Include all fields from the MDC. | `false` |
| maximumMessageSize| Maximum message size (in bytes). If the message size is exceeded, the appender will submit the message in multiple chunks. | `8192` |
| timestampPattern  | Date/time pattern for the `Time` field| `yyyy-MM-dd HH:mm:ss,SSSS` |

The only mandatory field is `host`. All other fields are optional.

log4j Configuration
--------------

Properties:

    log4j.appender.gelf=biz.paluch.logging.gelf.log4j.GelfLogAppender
    log4j.appender.gelf.Threshold=INFO
    log4j.appender.gelf.Host=udp:localhost
    log4j.appender.gelf.Port=12201
    log4j.appender.gelf.Version=1.0
    log4j.appender.gelf.Facility=java-test
    log4j.appender.gelf.ExtractStackTrace=true
    log4j.appender.gelf.FilterStackTrace=true
    log4j.appender.gelf.MdcProfiling=true
    log4j.appender.gelf.TimestampPattern=yyyy-MM-dd HH:mm:ss,SSSS
    log4j.appender.gelf.MaximumMessageSize=8192
    log4j.appender.gelf.AdditionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
    log4j.appender.gelf.MdcFields=mdcField1,mdcField2
    log4j.appender.gelf.DynamicMdcFields=mdc.*,(mdc|MDC)fields
    log4j.appender.gelf.IncludeFullMdc=true


XML:

    <appender name="gelf" class="biz.paluch.logging.gelf.log4j.GelfLogAppender">
        <param name="Threshold" value="INFO" />
        <param name="Host" value="udp:localhost" />
        <param name="Port" value="12201" />
        <param name="Facility" value="java-test" />
        <param name="ExtractStackTrace" value="true" />
        <param name="FilterStackTrace" value="true" />
        <param name="MdcProfiling" value="true" />
        <param name="TimestampPattern" value="yyyy-MM-dd HH:mm:ss,SSSS" />
        <param name="MaximumMessageSize" value="8192" />
        <param name="AdditionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
        <param name="MdcFields" value="mdcField1,mdcField2" />
        <param name="DynamicMdcFields" value="mdc.*,(mdc|MDC)fields" />
        <param name="IncludeFullMdc" value="true" />
    </appender>
    