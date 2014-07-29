# Default Fields

By default, following fields are used in the gelf message:

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

Currently only log4j2 provides a full configuration for every individual field. Other implementations can specify only
additional static/MDC fields. Therefore you can provide a property-file which specifies a mapping for the additional 
fields listed above.

The file must be named **default-logstash-fields.properties** and must be available on the class path. If the file is
not found, the default mapping (see above) is used. Typos/wrong field names are discarded quietly. Fields, that are not
listed in your mapping are not sent via gelf.

## Format
The format for the properties file is:

    targetFieldName=sourceFieldName

## Field names
You can use following field names (case insensitive, see also [biz.paluch.logging.gelf.LogMessageField.NamedLogField](apidocs/biz/paluch/logging/gelf/LogMessageField.NamedLogField.html) ) for your mapping:

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


**Example**

    # slightly different field names
    MyTime=Time
    MySeverity=Severity
    Thread=ThreadName
    SourceClassName=SourceClassName
    SourceMethodName=SourceMethodName
    Server=Server
    LoggerName=LoggerName
