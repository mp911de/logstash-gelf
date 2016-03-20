# HTTP transport for logstash-gelf 


logstash-gelf provides a HTTP/HTTPS transport to send log events to HTTP endpoints. The HTTP sender uses `POST` to
send uncompressed JSON data. It sets the `Content-type` header to `application/json` and expects response status `202 Accepted`.

    http://host[:port]/[path] (POST)

Example:

    http://localhost/gelf
    https://localhost/gelf

   * scheme    (fixed: http or https, directly used to determine the to be used sender class)
   * host      (variable: the host)
   * port      (variable: optional, the port. Defaults to 80 for HTTP and 443 for HTTPS)
   * path      (variable: optional, path on the server)
