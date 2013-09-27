package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfUDPSender implements GelfSender {
    private InetAddress host;
    private int port;
    private DatagramChannel channel;

    public GelfUDPSender() {
    }

    public GelfUDPSender(String host) throws IOException {
        this(host, DEFAULT_PORT);
    }

    public GelfUDPSender(String host, int port) throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.channel = initiateChannel();
    }

    private DatagramChannel initiateChannel() throws IOException {
        DatagramChannel resultingChannel = DatagramChannel.open();
        resultingChannel.socket().bind(new InetSocketAddress(0));
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
            return false;
        }

        return true;
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
