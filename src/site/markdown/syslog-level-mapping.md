# Syslog level mapping

Gelf requires a syslog level which is an integer. There are several ways how to determine that. log4j for example has an built-in
mapping. To keep the style consistent over various logging frameworks this is the way how it works:

## log4j2

Resolution of the syslog level is based on the `StandardLevel` property of the log level:

* FATAL: 2
* ERROR: 3
* WARN: 4
* INFO: 6
* Everything else: 7

## log4j 1.2, JUL, JBoss Logging, Logback

These frameworks provide an integer representation of the log level value:

* Level greater FATAL: 0
* Level is less or equal to FATAL: 2
* Level is greater to SEVERE: 2
* Level is greater to ERROR: 2
* Level is less or equal to ERROR or SEVERE: 3
* Level is less or equal to WARN: 4
* Level is less or equal to INFO: 6
* Level is less or equal to CONFIG/DEBUG: 7
