package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * (c) https://github.com/Batigoal/logstash-gelf.git
 */
public class DefaultGelfSenderProvider implements GelfSenderProvider {

    public static final int DEFAULT_PORT = 12201;

    @Override
    public boolean supports(String host) {
        return host != null;
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        String graylogHost = configuration.getHost();

        int port = configuration.getPort();
        if (port == 0) {
            port = DEFAULT_PORT;
        }

        if (graylogHost.startsWith("tcp:")) {
            int timeoutMs = (int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);
            String tcpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfTCPSender(tcpGraylogHost, port, timeoutMs, timeoutMs, configuration.getErrorReporter());
        } else if (graylogHost.startsWith("udp:")) {
            String udpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfUDPSender(udpGraylogHost, port, configuration.getErrorReporter());
        } else if (graylogHost.startsWith("http")) {
            return new GelfHTTPSender(graylogHost, port, configuration.getErrorReporter(), HttpClientBuilder.create());

        } else {
            return new GelfUDPSender(graylogHost, port, configuration.getErrorReporter());
        }

    }

}
