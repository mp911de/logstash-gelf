# TCP transport for logstash-gelf 


The TCP transport for logstash-gelf allows to configure TCP-specific options. Options are configured in a URI Query-String style:

**Plaintext**

    tcp:hostname?readTimeout=10s&connectionTimeout=1000ms&deliveryAttempts=5&keepAlive=true

**SSL**

    ssl:hostname?readTimeout=10s&connectionTimeout=1000ms&deliveryAttempts=5&keepAlive=true

## Options

* URI scheme specifies whether to use plaintext `tcp` or SSL `ssl`. 
* `readTimeout` Socket Read-Timeout (SO_TIMEOUT). The unit can be specified as suffix (see below). A timeout of zero is interpreted as an infinite timeout. Defaults to `2s`. 
* `connectionTimeout` Socket Connection-Timeout (SO_TIMEOUT). The unit can be specified as suffix (see below). A timeout of zero is interpreted as an infinite timeout. Defaults to `2s`.
* `deliveryAttempts` Number of Delivery-Attempts. Will retry to deliver the message and reconnect if necessary. A number of zero is interpreted as an infinite attempts. Defaults to `1`.   
* `keepAlive` Enable TCP keepAlive. Defaults to `false`.   

## SSL

TCP with SSL uses the JVM's truststore settings. You can specify a truststore by configuring the location to the truststore by setting `javax.net.ssl.trustStore`. Make sure this system property is set before bootstrapping any SSL-related components. Once the default SSL context is initialized, changes to `javax.net.ssl.trustStore` are not applied.
  
A customized `javax.net.ssl.SSLContext` can be provided by implementing an own `GelfSenderProvider` that configures `GelfTCPSSLSender`.

## Timeout Units

Timeout values can be specified with or without a time-unit. The time-unit is appended directly to the numeric timeout value. 
A not specified time-unit defaults to `MILLISECONDS`

Available time-units are:

* `ns` -- `NANOSECONDS`
* `us` -- `MICROSECONDS`
* `ms` -- `MILLISECONDS`
* `s` -- `SECONDS`
* `m` -- `MINUTES`
* `h` -- `HOURS`
* `d` -- `DAYS`


Examples:

* `2s` -- 2 seconds
* `1000` -- 1000 milliseconds
* `5m` -- 5 minutes

## Buffer size

The TCP sender uses pooled buffers to reduce GC pressure. The size defaults to `811.008` bytes (`99 * 8192`). The size can be controlled by setting the `logstash-gelf.buffer.size` property (system property). Buffering is disabled if `logstash-gelf.buffer.size` is set to `0` (zero). 
