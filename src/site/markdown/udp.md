# UDP transport for logstash-gelf 

    udp:hostname

## Buffer size

The TCP sender uses pooled buffers to reduce GC pressure. The size defaults to `811.008` bytes (`99 * 8192`). The size can be controlled by setting the `logstash-gelf.buffer.size` property (system property). Buffering is disabled if `logstash-gelf.buffer.size` is set to `0` (zero). 
