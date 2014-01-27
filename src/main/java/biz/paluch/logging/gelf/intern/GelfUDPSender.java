package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfUDPSender implements GelfSender {
    private InetAddress host;
    private int port;
    private DatagramChannel channel;
    private ErrorReporter errorReporter;

    public GelfUDPSender(String host, int port, ErrorReporter errorReporter) throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.errorReporter = errorReporter;
        this.channel = initiateChannel();
    }

    private DatagramChannel initiateChannel() throws IOException {
        DatagramChannel resultingChannel = DatagramChannel.open();

        try {
            resultingChannel.socket().bind(new InetSocketAddress(0));
        } catch (SocketException e) {
            errorReporter.reportError(e.getMessage(), e);
        }

        resultingChannel.connect(new InetSocketAddress(this.host, this.port));
        resultingChannel.configureBlocking(false);

        return resultingChannel;
    }

    public boolean sendMessage(GelfMessage message) {
        return message.isValid() && sendDatagrams(message.toUDPBuffers());
    }

    private boolean sendDatagrams(ByteBuffer[] bytesList) {
        try {
            for (ByteBuffer buffer : bytesList) {
                channel.write(buffer);
            }
        } catch (IOException e) {
            errorReporter.reportError(e.getMessage(), e);
            return false;
        }

        return true;
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            errorReporter.reportError(e.getMessage(), e);
        }
    }
}
