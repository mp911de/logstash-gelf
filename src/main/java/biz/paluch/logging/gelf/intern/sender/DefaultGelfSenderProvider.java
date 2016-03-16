package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static biz.paluch.logging.gelf.GelfUtil.addDefaultPortIfMissing;

/**
 * (c) https://github.com/Batigoal/logstash-gelf.git
 */
public class DefaultGelfSenderProvider implements GelfSenderProvider {

    public static final int DEFAULT_PORT = 12201;

    @Override public boolean supports(String host) {
        return host != null;
    }

    @Override public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        String graylogHost = configuration.getHost();

        int port = configuration.getPort();
        if (port == 0) {
            port = DEFAULT_PORT;
        }

        if (graylogHost.startsWith("tcp:")) {

            int defaultTimeoutMs = (int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);

            URI uri = URI.create(graylogHost);

            Map<String, String> params = UriParser.parse(uri);
            int connectionTimeMs = (int) UriParser.getTimeAsMs(params, GelfTCPSender.CONNECTION_TIMEOUT, defaultTimeoutMs);
            int readTimeMs = (int) UriParser.getTimeAsMs(params, GelfTCPSender.READ_TIMEOUT, defaultTimeoutMs);
            int deliveryAttempts = UriParser.getInt(params, GelfTCPSender.RETRIES, 1);
            boolean keepAlive = UriParser.getString(params, GelfTCPSender.KEEPALIVE, false);

            String tcpGraylogHost = UriParser.getHost(uri);
            return new GelfTCPSender(tcpGraylogHost, port, connectionTimeMs, readTimeMs, deliveryAttempts, keepAlive,
                    configuration.getErrorReporter());
        } else if (graylogHost.startsWith("udp:")) {
            URI uri = URI.create(graylogHost);
            String udpGraylogHost = UriParser.getHost(uri);
            return new GelfUDPSender(udpGraylogHost, port, configuration.getErrorReporter());
        } else if (graylogHost.startsWith("http")) {
            String graylogHostWithDefaultPort = addDefaultPortIfMissing(graylogHost, String.valueOf(port));
            URL url = new URL(graylogHostWithDefaultPort);
            return new GelfHTTPSender(url, configuration.getErrorReporter());

        } else {
            return new GelfUDPSender(graylogHost, port, configuration.getErrorReporter());
        }

    }

}
