# MDC

The Mapped Diagnostic Context, or MDC in short, is an instrument for distinguishing interleaved log output from different sources. 
The MDC (or ThreadContext in log4j2) is managed on a per thread basis.
 
logstash-gelf can extract values from the MDC and submit the values within the GELF message. There are various options
how to include specific/all MDC fields:

* Specify the name (names) of the field (fields) using the `mdcFields` property (e.g. `mdcFields=Application,Version,SomeOtherFieldName`)
* Include MDC fields based on one or more regular expression filters using the `dynamicMdcFields` property (e.g. `dynamicMdcFields=prefix.*,[a-z]+Field`)
* Include all MDC using the `includeFullMdc` property (e.g. `includeFullMdc=true`)

logstash-gelf will only use not empty fields (not null) and perform by default a type discovery. 
Fields that contain a floating point number (e.g. `3.1415`) are converted to a double/floating point number type.
Integer numbers (e.g. `42`) are converted to a long number type. Everything else is treated as a string.
 
## Specified types for additional fields

In some rare cases it's required to specify the data type of a particular field to always use the same data type.
This can be achieved by setting the `additionalFieldTypes` property (e.g. `additionalFieldTypes=field1=String,field2=long,field3=Long,field4=double,field5=Double`).
The type setting is only applied to additional (static) fields and MDC fields. System GELF fields are not affected by this setting.

The specified types apply only to the specified fields. Not specified fields follow the default rule, see above. Available types are:

* `String`
* `long`: Convert numbers (integer and floating-point) to an integer type. Use zero (`0`) if the value cannot be parsed into a number.
* `Long`: Convert numbers (integer and floating-point) to an integer type. Do not send this value (use `null`) if the value cannot be parsed into a number.
* `double`: Convert numbers (integer and floating-point) to a floating-point type. Use zero (`0.0`) if the value cannot be parsed into a number.
* `Double`: Convert numbers (integer and floating-point) to a floating-point type. Do not send this value (use `null`) if the value cannot be parsed into a number.
* `discover`: Apply default content type discovery, see above
