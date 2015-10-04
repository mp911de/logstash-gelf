# MDC Profiling

MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must set one value in the MDC:

profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)

Two values are set by the Log Appender:

 * profiling.requestEnd: End-Time of the Request-End in Date.toString-representation
 * profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)