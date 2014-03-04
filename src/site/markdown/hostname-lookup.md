Hostname-Lookup
--------------
logstash-gelf contains a class (biz.paluch.logging.RuntimeContainer) which performs a hostname resolution on the first use of the framework.
This can take some time, especially, when your DNS is not set up for all host addresses.

The lookup is performed in that way:

1. Retrieve all `InetAddress`es from all `NetworkInterface`s
2. Skip `LoopbackAdress`es
3. Skip all `InetAddress`es which cannot be resolved to a FQDN (`HostAddress` equals `CanonicalHostName`)
4. Use first `InetAddress` that passes that rules.
5. If none passes, use `LocalHost` `InetAddress`
6. If the lookup fails, hostname and FQDN hostname will be set to "unknown"


Bypassing Hostname-Lookup
-------------------------
You can bypass that process by setting some SystemProperties. Set `logstash-gelf.skipHostnameResolution` to `true` to bypass the resolution. If you've set the `originHost` property on
your logger, so the properties value will be used anyway. If you didn't set the `originHost` property, and lookup is disabled, the value "unknown" will be used as hostname.


Default/Fallback Values for Hostname-Lookup
--------------------------------------------

* Set `logstash-gelf.hostname` to use a fixed hostname (simple). This property can be handy in case the lookup runs into IO Exceptions.
* Set `logstash-gelf.fqdn.hostname` to use a fixed hostname (fully qualified). This property can be handy in case the lookup runs into IO Exceptions.
