logstash-gelf
=========================

[![Join the chat at https://gitter.im/mp911de/logstash-gelf](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mp911de/logstash-gelf?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://api.travis-ci.org/mp911de/logstash-gelf.svg)](https://travis-ci.org/mp911de/logstash-gelf)
[![codecov](https://codecov.io/gh/mp911de/logstash-gelf/branch/master/graph/badge.svg)](https://codecov.io/gh/mp911de/logstash-gelf)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/biz.paluch.logging/logstash-gelf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/biz.paluch.logging/logstash-gelf)

Provides logging to logstash using the Graylog Extended Logging Format ([GELF](http://www.graylog2.org/resources/gelf/specification) 1.0 and 1.1) for using with:

* [Java Util Logging](#jul)
* [Glassfish/Payara](#payara)
* [log4j 1.2.x](#log4j)
* [log4j 2.x](#log4j2)
* [JBoss AS7](#jbossas7)
* [WildFly](#wildfly)
* [Thorntail (WildFly Swarm 2.x)](#thorntail)
* [Logback](#logback)

See also http://logging.paluch.biz/ or http://www.graylog2.org/resources/gelf/specification for further documentation.


Including it in your project
--------------

Maven:
```xml
<dependency>
    <groupId>biz.paluch.logging</groupId>
    <artifactId>logstash-gelf</artifactId>
    <version>x.y.z</version>
</dependency>
```
    
Direct download from [Maven Central](http://search.maven.org/remotecontent?filepath=biz/paluch/logging/logstash-gelf/1.12.0/logstash-gelf-1.12.0.jar)    


JBoss AS/WildFly Module Download:

```xml
<dependency>
    <groupId>biz.paluch.logging</groupId>
    <artifactId>logstash-gelf</artifactId>
    <version>x.y.z</version>
    <classifier>logging-module</classifier>
</dependency>
```

Direct download from [Maven Central](http://search.maven.org/remotecontent?filepath=biz/paluch/logging/logstash-gelf/1.12.0/logstash-gelf-1.12.0-logging-module.zip)

Using snapshot builds:

```xml
<dependency>
    <groupId>biz.paluch.logging</groupId>
    <artifactId>logstash-gelf</artifactId>
    <version>x.y.z-SNAPSHOT</version>
</dependency>

<repositories>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

<a name="jul"/>

Java Util Logging GELF configuration
--------------

**Properties**

```properties
handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler

.handlers = biz.paluch.logging.gelf.jul.GelfLogHandler, java.util.logging.ConsoleHandler
.level = INFO

biz.paluch.logging.gelf.jul.GelfLogHandler.host=udp:localhost
biz.paluch.logging.gelf.jul.GelfLogHandler.port=12201
biz.paluch.logging.gelf.jul.GelfLogHandler.version=1.1
biz.paluch.logging.gelf.jul.GelfLogHandler.facility=java-test
biz.paluch.logging.gelf.jul.GelfLogHandler.extractStackTrace=true
biz.paluch.logging.gelf.jul.GelfLogHandler.filterStackTrace=true
biz.paluch.logging.gelf.jul.GelfLogHandler.timestampPattern=yyyy-MM-dd HH:mm:ss,SSS
biz.paluch.logging.gelf.jul.GelfLogHandler.maximumMessageSize=8192

# This are static fields
biz.paluch.logging.gelf.jul.GelfLogHandler.additionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
# Optional: Specify field types
biz.paluch.logging.gelf.jul.GelfLogHandler.additionalFieldTypes=fieldName1=String,fieldName2=Double,fieldName3=Long
biz.paluch.logging.gelf.jul.GelfLogHandler.level=INFO
```

Glassfish/Payara configuration
-------------
Install the library with its dependencies (see download above) in Glassfish. Place it below the `$GFHOME/glassfish/domains/$YOURDOMAIN/lib/ext/` path, then add the [Java Util Logging](#jul) to your `logging.properties` file.


<a name="payara"/>


**Properties**

```properties
log4j.appender.gelf=biz.paluch.logging.gelf.log4j.GelfLogAppender
log4j.appender.gelf.Threshold=INFO
log4j.appender.gelf.Host=udp:localhost
log4j.appender.gelf.Port=12201
log4j.appender.gelf.Version=1.1
log4j.appender.gelf.Facility=java-test
log4j.appender.gelf.ExtractStackTrace=true
log4j.appender.gelf.FilterStackTrace=true
log4j.appender.gelf.MdcProfiling=true
log4j.appender.gelf.TimestampPattern=yyyy-MM-dd HH:mm:ss,SSS
log4j.appender.gelf.MaximumMessageSize=8192

# This are static fields
log4j.appender.gelf.AdditionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
# Optional: Specify field types
log4j.appender.gelf.AdditionalFieldTypes=fieldName1=String,fieldName2=Double,fieldName3=Long

# This are fields using MDC
log4j.appender.gelf.MdcFields=mdcField1,mdcField2
log4j.appender.gelf.DynamicMdcFields=mdc.*,(mdc|MDC)fields
log4j.appender.gelf.IncludeFullMdc=true
```


**XML**

```xml
<appender name="gelf" class="biz.paluch.logging.gelf.log4j.GelfLogAppender">
    <param name="Threshold" value="INFO" />
    <param name="Host" value="udp:localhost" />
    <param name="Port" value="12201" />
    <param name="Version" value="1.1" />
    <param name="Facility" value="java-test" />
    <param name="ExtractStackTrace" value="true" />
    <param name="FilterStackTrace" value="true" />
    <param name="MdcProfiling" value="true" />
    <param name="TimestampPattern" value="yyyy-MM-dd HH:mm:ss,SSS" />
    <param name="MaximumMessageSize" value="8192" />
    
    <!-- This are static fields -->
    <param name="AdditionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
    <!-- Optional: Specify field types -->
    <param name="AdditionalFieldTypes" value="fieldName1=String,fieldName2=Double,fieldName3=Long" />
    
    <!-- This are fields using MDC -->
    <param name="MdcFields" value="mdcField1,mdcField2" />
    <param name="DynamicMdcFields" value="mdc.*,(mdc|MDC)fields" />
    <param name="IncludeFullMdc" value="true" />
</appender>
```

<a name="log4j2"/>

log4j2 GELF configuration
-------------------------

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

```xml
<Field name="fieldName1" literal="your literal value" />
```
    
### MDC Fields

```xml
<Field name="fieldName1" mdc="name of the MDC entry" />
```

### Dynamic MDC Fields

```xml
<DynamicMdcFields regex="mdc.*" />
```

In contrast to the configuration of other log frameworks log4j2 config uses one `DynamicMdcFields` element per regex (not separated by comma).

### Log-Event fields

See also: [Pattern Layout](http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout)

Set the desired pattern and the field will be sent using the specified pattern value. 

Additionally, you can add the host-Field, which can supply you either the FQDN hostname, the simple hostname or the local address.

Option | Description
--- | ---
host{["fqdn"<br/>"simple"<br/>"address"]} | Outputs either the FQDN hostname, the simple hostname or the local address. You can follow the throwable conversion word with an option in the form %host{option}. <br/> %host{fqdn} default setting, outputs the FQDN hostname, e.g. www.you.host.name.com. <br/>%host{simple} outputs simple hostname, e.g. www. <br/>%host{address} outputs the local IP address of the found hostname, e.g. 1.2.3.4 or affe:affe:affe::1. 

**XML**

```xml    
<Configuration packages="biz.paluch.logging.gelf.log4j2">
    <Appenders>
        <Gelf name="gelf" host="udp:localhost" port="12201" version="1.1" extractStackTrace="true"
              filterStackTrace="true" mdcProfiling="true" includeFullMdc="true" maximumMessageSize="8192"
              originHost="%host{fqdn}" additionalFieldTypes="fieldName1=String,fieldName2=Double,fieldName3=Long">
            <Field name="timestamp" pattern="%d{dd MMM yyyy HH:mm:ss,SSS}" />
            <Field name="level" pattern="%level" />
            <Field name="simpleClassName" pattern="%C{1}" />
            <Field name="className" pattern="%C" />
            <Field name="server" pattern="%host" />
            <Field name="server.fqdn" pattern="%host{fqdn}" />
            
            <!-- This is a static field -->
            <Field name="fieldName2" literal="fieldValue2" />
             
            <!-- This is a field using MDC -->
            <Field name="mdcField2" mdc="mdcField2" /> 
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
```    

<a name="jbossas7"/>

JBoss AS7 configuration
-----------------------
Include the library as module (see download above), then add following lines to your configuration:

**standalone.xml**

```xml
<custom-handler name="GelfLogger" class="biz.paluch.logging.gelf.jboss7.JBoss7GelfLogHandler" module="biz.paluch.logging">
    <level name="INFO" />
    <properties>
        <property name="host" value="udp:localhost" />
        <property name="port" value="12201" />
        <property name="version" value="1.1" />
        <property name="facility" value="java-test" />
        <property name="extractStackTrace" value="true" />
        <property name="filterStackTrace" value="true" />
        <property name="mdcProfiling" value="true" />
        <property name="timestampPattern" value="yyyy-MM-dd HH:mm:ss,SSS" />
        <property name="maximumMessageSize" value="8192" />
        
        <!-- This are static fields -->
        <property name="additionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
        <!-- Optional: Specify field types -->
        <property name="additionalFieldTypes" value="fieldName1=String,fieldName2=Double,fieldName3=Long" />
        
        <!-- This are fields using MDC -->
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
```

<a name="wildfly"/>

WildFly configuration
--------------------------------------------
Include the library as module (see download above). Place it below the `$JBOSS_HOME/modules/system/layers/base` path, then add following lines to your configuration:

standalone.xml
```xml
<custom-handler name="GelfLogger" class="biz.paluch.logging.gelf.wildfly.WildFlyGelfLogHandler" module="biz.paluch.logging">
    <level name="INFO" />
    <properties>
        <property name="host" value="udp:localhost" />
        <property name="port" value="12201" />
        <property name="version" value="1.1" />
        <property name="facility" value="java-test" />
        <property name="extractStackTrace" value="true" />
        <property name="filterStackTrace" value="true" />
        <property name="mdcProfiling" value="true" />
        <property name="timestampPattern" value="yyyy-MM-dd HH:mm:ss,SSS" />
        <property name="maximumMessageSize" value="8192" />
        
        <!-- This are static fields -->
        <property name="additionalFields" value="fieldName1=fieldValue1,fieldName2=fieldValue2" />
        <!-- Optional: Specify field types -->
        <property name="additionalFieldTypes" value="fieldName1=String,fieldName2=Double,fieldName3=Long" />
        
        <!-- This are fields using MDC -->
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
```

<a name="thorntail"/>

Thorntail (WildFly Swarm 2.x) configuration
--------------------------------------------
Include `module.xml` from the logging module zip (see download above). Place it below the `src/main/resources/modules/biz/paluch/logging/main` path, then add following lines to your `project-stages.yml`:

`project-stages.yml`:
```yaml
swarm:
  logging:
    custom-handlers:
      GelfLogger:
        attribute-class: biz.paluch.logging.gelf.wildfly.WildFlyGelfLogHandler
        module: biz.paluch.logging
        properties:
            host=udp:localhost
            port=12201
            version=1.0
            facility=java-test
            extractStackTrace=true
            filterStackTrace=true
            includeLocation=true
            mdcProfiling=true
            timestampPattern=yyyy-MM-dd HH:mm:ss,SSS
            maximumMessageSize=8192
            additionalFields=fieldName1=fieldValue1,fieldName2=fieldValue2
            additionalFieldTypes=fieldName1=String,fieldName2=Double,fieldName3=Long
            MdcFields=mdcField1,mdcField2
            dynamicMdcFields=mdc.*,(mdc|MDC)fields
            includeFullMdc=true
    root-logger:
      level: INFO
      handlers:
      - GelfLogger
```

<a name="logback"/>

Logback GELF configuration
--------------------------
logback.xml Example:

```xml
<!DOCTYPE configuration>

<configuration>
    <contextName>test</contextName>
    <jmxConfigurator/>

    <appender name="gelf" class="biz.paluch.logging.gelf.logback.GelfLogbackAppender">
        <host>udp:localhost</host>
        <port>12201</port>
        <version>1.1</version>
        <facility>java-test</facility>
        <extractStackTrace>true</extractStackTrace>
        <filterStackTrace>true</filterStackTrace>
        <mdcProfiling>true</mdcProfiling>
        <timestampPattern>yyyy-MM-dd HH:mm:ss,SSS</timestampPattern>
        <maximumMessageSize>8192</maximumMessageSize>
        
        <!-- This are static fields -->
        <additionalFields>fieldName1=fieldValue1,fieldName2=fieldValue2</additionalFields>
        <!-- Optional: Specify field types -->
        <additionalFieldTypes>fieldName1=String,fieldName2=Double,fieldName3=Long</additionalFieldTypes>
        
        <!-- This are fields using MDC -->
        <mdcFields>mdcField1,mdcField2</mdcFields>
        <dynamicMdcFields>mdc.*,(mdc|MDC)fields</dynamicMdcFields>
        <includeFullMdc>true</includeFullMdc>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
    </appender>

    <root level="DEBUG">
        <appender-ref ref="gelf" />
    </root>
</configuration>
```

Versions/Dependencies
---------------------
This project is built against following dependencies/versions:

* log4j 1.2.14
* log4j2 2.9.1
* Java Util Logging JDK Version 1.6
* logback 1.1.3
* slf4j-api 1.7.13
* jedis 2.9.0 (includes commons-pool2 2.4.3)

License
-------
* [The MIT License (MIT)](http://opensource.org/licenses/MIT)
* Contains also code from https://github.com/t0xa/gelfj

Contributing
------------
Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in and take a look into [CONTRIBUTING.md](https://github.com/mp911de/logstash-gelf/blob/master/.github/CONTRIBUTING.md)
