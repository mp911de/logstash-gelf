# Kafka transport for logstash-gelf 


logstash-gelf can be used together with [Kafka](https://kafka.apache.org/) for shipping log events. 

 The URI used as connection property is a java.net.URI. The minimal URI must contain at least a host and
 the Fragment (Topic Name). 
 The URL allows to specify one or more brokers comma separated but in this case you must define ports inside URL.

    kafka://broker[:port]?[producer_properties]#[log-topic]

Example:

    kafka://localhost#topic-log
    kafka://localhost:9092#topic-log
    kafka://localhost:9092,localhost:9093,localhost:9094#topic-log
    kafka://localhost?acks=all#topic-log
    kafka://localhost:19092?acks=1&max.block.ms=1000&transaction.timeout.ms=1000&request.timeout.ms=1000#kafka-log-topic


   * scheme    (fixed: Kafka, directly used to determine the to be used sender class)
   * host      (variable: the host your Kafka broker runs on)
   * port      (variable: the port your Kafka broker runs on)
   * query     (variable: kafka producer config properties which is usually defined inside producer.properties file)
   * fragment  (variable: the topic we send log messages on)

**Limitations**

Some configurations will be overridden or set by default:
- acks (If you set it to 0 it will be set to 1 by default for log message acknowledgements) defaultValue = all
- retries defaultValue=2
- value.serializer ByteArraySerializer (will always override)
- key.serializer ByteArraySerializer (will always override)

**When using with SL4J/Logback/Spring:**

When you are using logback/sl4j/spring you must not use kafka sender for loggers of `org.apache.kafka` and `javax.management` 
packages this will create a cyclic dependency for [KafkaProducer](https://kafka.apache.org/20/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html) which is also using loggers from these package. 
You can use other logstash-gelf appender such as UDP or TCP to log these logs to Graylog.

You can set these loggers by changing appender-ref this way:
```xml
<logger name="org.apache.kafka" level="ALL" additivity="false">
     <appender-ref ref="gelfUdp" />
     <appender-ref ref="STDOUT" />
</logger>
<logger name="javax.management" level="ALL" additivity="false">
     <appender-ref ref="gelfUdp" />
     <appender-ref ref="STDOUT" />
</logger>
```