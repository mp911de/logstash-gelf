package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import biz.paluch.logging.gelf.GelfMessageAssembler;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:12
 */
public class GelfSenderFactory {

    public static GelfSender createSender(GelfMessageAssembler gelfMessageAssembler, ErrorReporter errorReporter) {
        if (gelfMessageAssembler.getHost() == null) {
            errorReporter.reportError("Graylog2 hostname is empty!", null);
        } else {
            try {
                return createSender(gelfMessageAssembler.getHost(), gelfMessageAssembler.getPort(), errorReporter);
            } catch (UnknownHostException e) {
                errorReporter.reportError("Unknown Graylog2 hostname:" + gelfMessageAssembler.getHost(), e);
            } catch (SocketException e) {
                errorReporter.reportError("Socket exception: " + e.getMessage(), e);
            } catch (IOException e) {
                errorReporter.reportError("IO exception: " + e.getMessage(), e);
            }
        }

        return null;
    }

    public static GelfSender createSender(String graylogHost, int graylogPort, ErrorReporter errorReporter) throws IOException {

        if (graylogHost.startsWith("tcp:")) {
            String tcpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfTCPSender(tcpGraylogHost, graylogPort, errorReporter);
        } else if (graylogHost.startsWith("udp:")) {
            String udpGraylogHost = graylogHost.substring(4, graylogHost.length());
            return new GelfUDPSender(udpGraylogHost, graylogPort, errorReporter);
        } else {
            return new GelfUDPSender(graylogHost, graylogPort, errorReporter);
        }

    }
}
