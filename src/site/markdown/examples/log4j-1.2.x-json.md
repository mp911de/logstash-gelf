log4j 1.2.x JSON Layout
=========

Following settings can be used:

| Attribute Name    | Description                          | Default |
| ----------------- |:------------------------------------:|:-------:|
| lineBreak         | End of line string | platform dependent default value, see `System.getProperty("line.separator")` |
| fields            | Comma-separated list of log event fields that should be included in the JSON | `Time, Severity, ThreadName, SourceClassName, SourceMethodName, SourceSimpleClassName, LoggerName, NDC, Server` |
| version           | GELF Version `1.0` or `1.1` | `1.0` |
| originHost        | Originating Hostname  | FQDN Hostname |
| extractStackTrace | Send the Stack-Trace to the StackTrace field (`true`/`false`)  | `false` |
| filterStackTrace  | Perform Stack-Trace filtering (`true`/`false`)| `false` |
| includeLocation   | Include source code location | `true` |
| mdcProfiling      | Perform Profiling (Call-Duration) based on MDC Data. See [MDC Profiling](../mdcprofiling.html) for details  | `false` |
| facility          | Name of the Facility  | `logstash-gelf` |
| additionalFields  | Send additional static fields. The fields are specified as key-value pairs are comma-separated. Example: `GelfLogHandler.additionalFields=fieldName=Value,fieldName2=Value2` | none |
| additionalFieldTypes | Type specification for additional and MDC fields. Supported types: `String`, `long`, `Long`, `double`, `Double` and `discover` (default if not specified, discover field type on parseability). Eg. field=String,field2=double | `discover` for all additional fields |
| mdcFields         | Send additional fields whose values are obtained from MDC. Name of the Fields are comma-separated. Example: `mdcFields=Application,Version,SomeOtherFieldName` | none |
| dynamicMdcFields  | Dynamic MDC Fields allows you to extract MDC values based on one or more regular expressions. Multiple regexes are comma-separated. The name of the MDC entry is used as GELF field name. | none |
| includeFullMdc    | Include all fields from the MDC. | `false` |
| timestampPattern  | Date/time pattern for the `Time` field| `yyyy-MM-dd HH:mm:ss,SSS` |

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
    "facility": "logstash-gelf",
    "timestamp": "1439321132.734"
}
```

Default line break depends on the platform, JSON objects are separated by line breaks only.


log4j Configuration
--------------

Properties:

    log4j.appender.file.layout=biz.paluch.logging.gelf.log4j.GelfLayout
    log4j.appender.file.layout.Version=1.0
    log4j.appender.file.layout.Facility=logstash-gelf
    log4j.appender.file.layout.ExtractStackTrace=true
    log4j.appender.file.layout.FilterStackTrace=true
    log4j.appender.file.layout.IncludeLocation=true
    log4j.appender.file.layout.MdcProfiling=true
    log4j.appender.file.layout.TimestampPattern=yyyy-MM-dd HH:mm:ss,SSS
    log4j.appender.file.layout.AdditionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
    log4j.appender.file.layout.AdditionalFieldTypes=fieldName1=String,fieldName2=Double,fieldName3=Long
    log4j.appender.file.layout.MdcFields=mdcField1,mdcField2
    log4j.appender.file.layout.DynamicMdcFields=mdc.*,(mdc|MDC)fields
    log4j.appender.file.layout.IncludeFullMdc=true


XML:

    <appender name="file" class="org.apache.log4j.FileAppender">
        <param name="file" value="logfile.log" />
        <layout class="biz.paluch.logging.gelf.log4j.GelfLayout">
            <param name="Facility" value="logstash-gelf" />
            <param name="ExtractStackTrace" value="true" />
            <param name="FilterStackTrace" value="true" />
            <param name="MdcProfiling" value="true" />
            <param name="IncludeLocation" value="true" />
            <param name="TimestampPattern" value="yyyy-MM-dd HH:mm:ss,SSS" />
            <param name="AdditionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
            <param name="AdditionalFieldTypes" value="fieldName1=String,fieldName2=Double,fieldName3=Long" />
            <param name="MdcFields" value="mdcField1,mdcField2" />
            <param name="DynamicMdcFields" value="mdc.*,(mdc|MDC)fields" />
            <param name="IncludeFullMdc" value="true" />
        </layout>
    </appender>
