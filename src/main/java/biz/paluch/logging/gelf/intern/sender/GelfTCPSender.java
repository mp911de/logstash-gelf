package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfTCPSender implements GelfSender {
    private boolean shutdown = false;
    private InetAddress host;
    private int port;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private Socket socket;
    private ErrorReporter errorReporter;

    public GelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
            throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.errorReporter = errorReporter;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown) {
            return false;
        }

        try {
            // reconnect if necessary
            if (socket == null) {
                socket = createSocket();
            }

            socket.getOutputStream().write(message.toTCPBuffer().array());

            return true;
        } catch (IOException e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send data to " + host + ":" + port, e));
            // if an error occours, signal failure
            socket = null;
            return false;
        }
    }

    protected Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(readTimeoutMs);
        socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
        return socket;
    }

    public void close() {
        shutdown = true;
        Closer.close(socket);
    }
}
