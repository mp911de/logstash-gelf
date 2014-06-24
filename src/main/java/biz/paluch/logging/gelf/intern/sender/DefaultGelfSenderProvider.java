package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * 
 * (c) https://github.com/Batigoal/logstash-gelf.git
 * 
 */
public class DefaultGelfSenderProvider implements GelfSenderProvider {

    public static final int DEFAULT_PORT = 12201;

    @Override
    public boolean supports(String host) {
        return true;
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        String graylogHost = configuration.getHost();

        int port = configuration.getPort();
        if (port == 0) {
            port = DEFAULT_PORT;
        }

        if (graylogHost.startsWith("tcp:")) {
            String tcpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfTCPSender(tcpGraylogHost, port, configuration.getErrorReport());
        } else if (graylogHost.startsWith("udp:")) {
            String udpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfUDPSender(udpGraylogHost, port, configuration.getErrorReport());
        } else {
            return new GelfUDPSender(graylogHost, port, configuration.getErrorReport());
        }

    }

}
