# TCP transport for logstash-gelf 


The TCP transport for logstash-gelf allows to configure TCP-specific options. Options are configured in a URI Query-String style:

    tcp:hostname?readTimeout=10s&connectionTimeout=1000ms&deliveryAttempts=5&keepAlive=true

## Options

* `readTimeout` Socket Read-Timeout (SO_TIMEOUT). The unit can be specified as suffix (see below). A timeout of zero is interpreted as an infinite timeout. Defaults to `2s`. 
* `connectionTimeout` Socket Connection-Timeout (SO_TIMEOUT). The unit can be specified as suffix (see below). A timeout of zero is interpreted as an infinite timeout. Defaults to `2s`.
* `deliveryAttempts` Number of Delivery-Attempts. Will retry to deliver the message and reconnect if necessary. A number of zero is interpreted as an infinite attempts. Defaults to `1`.   
* `keepAlive` Enable TCP keepAlive. Defaults to `false`.   


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