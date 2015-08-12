Settings
--------------
Following settings can be used:

 * lineBreak (Optional): End of line, defaults to `\n`
 * fields (Optional): Comma-separated list of log event fields that should be included in the JSON. Defaults to `Time, Severity, ThreadName, SourceClassName, SourceMethodName, SourceSimpleClassName, LoggerName, NDC`
 * version (Optional): GELF Version 1.0 or 1.1, default 1.0
 * originHost (Optional): Originating Hostname, default FQDN Hostname
 * extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false
 * filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false
 * mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See MDC Profiling, default false. See [MDC Profiling](../mdcprofiling.html) for details.
 * facility (Optional): Name of the Facility, default logstash-gelf
 * threshold/level (Optional): Log-Level, default INFO
 * filter (Optional): Class-Name of a Log-Filter, default none
 * additionalFields (Optional): Post additional fields. Eg. fieldName=Value,fieldName2=Value2
 * mdcFields (Optional): Post additional fields, pull Values from MDC. Name of the Fields are comma-separated mdcFields=Application,Version,SomeOtherFieldName
 * dynamicMdcFields (Optional): Dynamic MDC Fields allows you to extract MDC values based on one or more regular expressions. Multiple regex are comma-separated. The name of the MDC entry is used as GELF field name.
 * includeFullMdc (Optional): Include all fields from the MDC, default false


The JSON formatter creates then messages like:

```
{
    "LoggerName": "org.wildfly.extension.undertow",
    "short_message": "WFLYUT0008: Undertow HTTP listener default suspending",
    "level": "6",
    "Time": "2015-08-11 21:25:32,0734",
    "Severity": "INFO",
    "MessageParam1": "default",
    "Thread": "MSC service thread 1-6",
    "SourceMethodName": "stopListening",
    "MessageParam0": "HTTP",
    "full_message": "WFLYUT0008: Undertow HTTP listener default suspending",
    "SourceSimpleClassName": "HttpListenerService",
    "SourceClassName": "org.wildfly.extension.undertow.HttpListenerService",
    "facility": "java-test",
    "timestamp": "1439321132.734"
}
```

Default line break is `\n`, JSON objects are separated by line breaks only.

WildFly 8/WildFly 9/WildFly 10 Logging configuration
--------------

XML Configuration:

    <formatter name="JsonFormatter">
        <custom-formatter module="biz.paluch.logging" class="biz.paluch.logging.gelf.wildfly.WildFlyJsonFormatter">
            <properties>
                <property name="version" value="1.0" />
                <property name="facility" value="java-test" />
                <property name="fields" value="Time,Severity,ThreadName,SourceClassName,SourceMethodName,SourceSimpleClassName,LoggerName,NDC" />
                <property name="extractStackTrace" value="true" />
                <property name="filterStackTrace" value="true" />
                <property name="mdcProfiling" value="true" />
                <property name="timestampPattern" value="yyyy-MM-dd HH:mm:ss,SSSS" />
                <property name="additionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
                <property name="mdcFields" value="mdcField1,mdcField2" />
                <property name="dynamicMdcFields" value="mdc.*,(mdc|MDC)fields" />
                <property name="includeFullMdc" value="true" />
            </properties>
        </custom-handler>
    </formatter>

    <file-handler name="JsonLog">
        <file relative-to="jboss.server.log.dir" path="server.json"/>
    </file-handler>
    ...

    <root-logger>
        <level name="INFO"/>
        <handlers>
            <handler name="FILE"/>
            <handler name="CONSOLE"/>
            <handler name="JsonLog"/>
        </handlers>
    </root-logger>


CLI Configuration:

    /subsystem=logging/custom-formatter=JsonFormatter/:add(module=biz.paluch.logging,class=biz.paluch.logging.gelf.wildfly.WildFlyJsonFormatter,properties={ \
           version="1.0", \
		   facility="java-test", \
		   fields="Time,Severity,ThreadName,SourceClassName,SourceMethodName,SourceSimpleClassName,LoggerName,NDC", \
		   extractStackTrace=true, \
		   filterStackTrace=true, \
		   mdcProfiling=true, \
		   timestampPattern="yyyy-MM-dd HH:mm:ss,SSSS", \
		   additionalFields="fieldName1=fieldValue1,fieldName2=fieldValue2", \
		   mdcFields="mdcField1,mdcField2" \
		   dynamicMdcFields="mdc.*,(mdc|MDC)fields" \
		   includeFullMdc=true \
    })
    
    /subsystem=logging/file-handler=JsonLog/:add(file={"relative-to"=>"jboss.server.log.dir", path=server.json}, \
            level=ALL,named-formatter=JsonFormatter)

    /subsystem=logging/root-logger=ROOT/:add-handler(name=JsonLog)
    
