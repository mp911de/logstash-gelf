log4j2
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
| includeFullMdc    | Include all fields from the MDC. | `false` |
| maximumMessageSize| Maximum message size (in bytes). If the message size is exceeded, the appender will submit the message in multiple chunks. | `8192` |

The only mandatory field is `host`. All other fields are optional.

### Fields

Log4j v2 supports an extensive and flexible configuration in contrast to other log frameworks (JUL, log4j v1). This allows you to specify your needed fields you want to use in the GELF message. An empty field configuration results in a message containing only

 * timestamp
 * level (syslog level)
 * host
 * facility
 * message
 * short_message

You can add different fields:

 * Static Literals
 * MDC Fields
 * Log-Event fields (using Pattern Layout)

In order to do so, use nested Field elements below the Appender element.

### Static Literals

    <Field name="fieldName1" literal="your literal value" />
    
### MDC Fields

    <Field name="fieldName1" mdc="name of the MDC entry" />

### Dynamic MDC Fields

    <DynamicMdcFields regex="mdc.*" />

In contrast to the configuration of other log frameworks log4j2 config uses one `DynamicMdcFields` element per regex (not separated by comma).
    
### Log-Event fields

See also: [Pattern Layout](http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout)

Set the desired pattern and the field will be sent using the specified pattern value. 

Additionally, you can add the host-Field, which can supply you either the FQDN hostname, the simple hostname or the local address.

Option | Description
--- | ---
host{["fqdn"<br/>"simple"<br/>"address"]} | Outputs either the FQDN hostname, the simple hostname or the local address. You can follow the throwable conversion word with an option in the form %host{option}. <br/> %host{fqdn} default setting, outputs the FQDN hostname, e.g. www.you.host.name.com. <br/>%host{simple} outputs simple hostname, e.g. www. <br/>%host{address} outputs the local IP address of the found hostname, e.g. 1.2.3.4 or affe:affe:affe::1. 

XML:
    
    <Configuration>
        <Appenders>
            <Gelf name="gelf" host="udp:localhost" port="12201" version="1.0" extractStackTrace="true"
                  filterStackTrace="true" mdcProfiling="true" includeFullMdc="true" maximumMessageSize="8192" 
                  originHost="my.host.name">
                <Field name="timestamp" pattern="%d{dd MMM yyyy HH:mm:ss,SSS}" />
                <Field name="level" pattern="%level" />
                <Field name="simpleClassName" pattern="%C{1}" />
                <Field name="className" pattern="%C" />
                <Field name="server" pattern="%host" />
                <Field name="server.fqdn" pattern="%host{fqdn}" />
                <Field name="fieldName2" literal="fieldValue2" /> <!-- This is a static field -->
                <Field name="mdcField2" mdc="mdcField2" /> <!-- This is a field using MDC -->
                <DynamicMdcFields regex="mdc.*" />
                <DynamicMdcFields regex="(mdc|MDC)fields" />
            </Gelf>
        </Appenders>
        <Loggers>
            <Root level="INFO">
                <AppenderRef ref="gelf" />
            </Root>
        </Loggers>
    </Configuration>
      
      