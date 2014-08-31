# Default Fields

This are the fields used in GELF messages:

**Default Fields**

* host
* short_message
* full_message
* timestamp (Java millis/1000)
* level (syslog severity)
* facility

**Additional Fields (prefixes with underscore within the protocol message)**

* Time (configurable timestamp)
* Severity (Log-Level)
* Thread (Thread name)
* SourceClassName
* SourceSimpleClassName (Simple source class name)
* SourceMethodName
* Server
* LoggerName (Name of the logger/category)
* Marker (logback/log4j2)
* NDC (JBossAS7/log4j 1.2.x)

## Override

log4j2 currently only provides a full configuration for every individual field. Other implementations can specify only
additional static/MDC fields since they follow in general a formatter/pattern concept. GELF messages contain a set of fields.
Users of log4j/JUL/logback can take control over default fields providing the default-logstash-fields.properties` configuration.

## default-logstash-fields.properties configuration

The file must be named `default-logstash-fields.properties` and must be available on the class path within the default
package. If no `default-logstash-fields.properties` is found, then the default mapping (see above) are used. 
Typos/wrong field names are discarded quietly. Fields, that are not listed in your mapping are not sent via GELF.

## Format
The format follows the Java Properties format:

    targetFieldName=sourceFieldName

## Field names
Available field names are (case insensitive, see also [biz.paluch.logging.gelf.LogMessageField.NamedLogField](apidocs/biz/paluch/logging/gelf/LogMessageField.NamedLogField.html) ):

* Time
* Severity
* ThreadName
* SourceClassName
* SourceSimpleClassName
* SourceMethodName
* Server
* LoggerName
* Marker
* NDC

## Example


```
# slightly different field names
MyTime=Time
MySeverity=Severity
Thread=ThreadName
SourceClassName=SourceClassName
SourceMethodName=SourceMethodName
Server=Server
LoggerName=LoggerName
```

## Default settings

```
Time=Time
Severity=Severity
ThreadName=ThreadName
SourceClassName=SourceClassName
SourceSimpleClassName=SourceSimpleClassName
SourceMethodName=SourceMethodName
Server=Server
LoggerName=LoggerName
Marker=Marker
NDC=NDC
```

## Verbose logging

You can turn on verbose logging to inspect the discovery of `default-logstash-fields.properties` by 
setting the system property `logstash-gelf.LogMessageField.verbose` to `true`.
 
