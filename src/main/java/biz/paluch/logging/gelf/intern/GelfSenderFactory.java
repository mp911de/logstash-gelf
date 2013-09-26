package biz.paluch.logging.gelf.intern;

import java.io.IOException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:12
 */
public class GelfSenderFactory {
    public static GelfSender createSender(String graylogHost, int graylogPort) throws IOException {

        if (graylogHost.startsWith("tcp:")) {
            String tcpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfTCPSender(tcpGraylogHost, graylogPort);
        } else if (graylogHost.startsWith("udp:")) {
            String udpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfUDPSender(udpGraylogHost, graylogPort);
        } else {
            return new GelfUDPSender(graylogHost, graylogPort);
        }

    }
}
