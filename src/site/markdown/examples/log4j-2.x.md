Settings
--------------
Following settings can be used:

 * host (since 1.2, Mandatory): Hostname/IP-Address of the Logstash Host
     * tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com
     * udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com
     * redis://\[:REDISDB_PASSWORD@\]REDISDB_HOST:REDISDB_PORT/REDISDB_NUMBER#REDISDB_LISTNAME , e.g. redis://:donttrustme@127.0.0.1:6379/0#myloglist or if no password needed redis://127.0.0.1:6379/0#myloglist
     * (the host) for UDP, e.g. 127.0.0.1 or some.host.com
 * port (since 1.2, Optional): Port, default 12201
 * graylogHost (until 1.1, Mandatory): Hostname/IP-Address of the Logstash Host
 * graylogPort (until 1.1, Optional): Port, default 12201
 * originHost (Optional): Originating Hostname, default FQDN Hostname
 * extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false
 * filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false
 * mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See MDC Profiling, default false. See [MDC Profiling](../mdcprofiling.html) for details.
 * facility (Optional): Name of the Facility, default logstash-gelf
 * includeFullMdc (Optional): Include all fields from the MDC, default false

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
            <Gelf name="gelf" graylogHost="udp:localhost" graylogPort="12201" extractStackTrace="true"
                  filterStackTrace="true" mdcProfiling="true" includeFullMdc="true" maximumMessageSize="8192">
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
      
      