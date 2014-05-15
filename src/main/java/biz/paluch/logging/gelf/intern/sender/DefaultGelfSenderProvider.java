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

    @Override
    public boolean supports(String host) {
        return true;
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        String graylogHost = configuration.getHost();

        if (graylogHost.startsWith("tcp:")) {
            String tcpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfTCPSender(tcpGraylogHost, configuration.getPort(), configuration.getErrorReport());
        } else if (graylogHost.startsWith("udp:")) {
            String udpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfUDPSender(udpGraylogHost, configuration.getPort(), configuration.getErrorReport());
        } else {
            return new GelfUDPSender(graylogHost, configuration.getPort(), configuration.getErrorReport());
        }

    }

}
