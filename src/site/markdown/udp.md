# UDP transport for logstash-gelf 

    udp:hostname

## Buffer size

The TCP sender uses pooled buffers to reduce GC pressure. The size defaults to `811.008` bytes (`99 * 8192`). The size can be controlled by setting the `logstash-gelf.buffer.size` property (system property). Buffering is disabled if `logstash-gelf.buffer.size` is set to `0` (zero). 

## Maximum Message Size

IP packets are subject to packet fragmentation during transport unless the [DF bit](https://tools.ietf.org/html/rfc4459) is set or an active network component isn't capable of fragmentation. In such cases, make sure to adjust the `maximumMessageSize` to your MTU size with considering a 12 bytes GELF header.  
