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

You can take control over the lookup order. Sometimes your host's have a real address on the localhost but a cluster name on the network devices and sometimes the other 
way arournd.

Controlling lookup order
-------------------------
You can provide a system property, `logstash-gelf.resolutionOrder` to control the lookup order.
 
* Use `localhost,network` for: First inspect the local host name, then try to get the host name from network devices. 
* Use `network,localhost` for: First inspect the network devices to retrieve a host name, then try to get the host name from the local host.

Bypassing Hostname-Lookup
-------------------------
You can bypass that process by setting some SystemProperties. Set `logstash-gelf.skipHostnameResolution` to `true` to bypass the resolution. If you've set the `originHost` property on
your logger, so the properties value will be used anyway. If you didn't set the `originHost` property, and lookup is disabled, the value "unknown" will be used as hostname.


Default/Fallback Values for Hostname-Lookup
--------------------------------------------

* Set `logstash-gelf.hostname` to use a fixed hostname (simple). This property can be handy in case the lookup runs into IO Exceptions.
* Set `logstash-gelf.fqdn.hostname` to use a fixed hostname (fully qualified). This property can be handy in case the lookup runs into IO Exceptions.

See [Javadoc](apidocs/biz/paluch/logging/RuntimeContainerProperties.html) for further details.
