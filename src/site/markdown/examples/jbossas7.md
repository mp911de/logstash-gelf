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
 * additionalFields (Optional): Post additional fields. Eg. fieldName=Value,fieldName2=Value2
 * mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated mdcFields=Application,Version,SomeOtherFieldName
 * dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name.
 * includeFullMdc (Optional): Include all fields from the MDC, default false


JBoss AS7/Wildfly Logging configuration
--------------

XML Configuration:

     <custom-handler name="GelfLogger" class="biz.paluch.logging.gelf.jboss7.JBoss7GelfLogHandler" module="biz.paluch.logging">
        <level name="INFO" />
        <properties>
            <property name="host" value="udp:localhost" />
            <property name="port" value="12201" />
            <property name="version" value="1.0" />
            <property name="facility" value="java-test" />
            <property name="extractStackTrace" value="true" />
            <property name="filterStackTrace" value="true" />
            <property name="mdcProfiling" value="true" />
            <property name="timestampPattern" value="yyyy-MM-dd HH:mm:ss,SSSS" />
            <property name="maximumMessageSize" value="8192" />
            <property name="additionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
            <property name="mdcFields" value="mdcField1,mdcField2" />
            <property name="dynamicMdcFields" value="mdc.*,(mdc|MDC)fields" />
            <property name="includeFullMdc" value="true" />
        </properties>
    </custom-handler>

    ...

    <root-logger>
        <level name="INFO"/>
        <handlers>
            <handler name="FILE"/>
            <handler name="CONSOLE"/>
            <handler name="GelfLogger"/>
        </handlers>
    </root-logger>


CLI Configuration:

    /subsystem=logging/custom-handler=GelfLogger/:add(module=biz.paluch.logging,class=biz.paluch.logging.gelf.jboss7.JBoss7GelfLogHandler,properties={ \
           host="udp:localhost", \
           port="udp:12201", \
           version="1.0", \
		   facility="java-test", \
		   extractStackTrace=true, \
		   filterStackTrace=true, \
		   mdcProfiling=true, \
		   timestampPattern="yyyy-MM-dd HH:mm:ss,SSSS", \
		   maximumMessageSize=8192, \
		   additionalFields="fieldName1=fieldValue1,fieldName2=fieldValue2", \
		   mdcFields="mdcField1,mdcField2" \
		   dynamicMdcFields="mdc.*,(mdc|MDC)fields" \
		   includeFullMdc=true \
    })

    /subsystem=logging/root-logger=ROOT/:write-attribute(name=handlers,value=["FILE","CONSOLE","GelfLogger"])
    