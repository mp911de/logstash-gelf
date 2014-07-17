package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.Closer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfTCPSender implements GelfSender {
    private boolean shutdown = false;
    private InetAddress host;
    private int port;
    private Socket socket;
    private ErrorReporter errorReporter;

    public GelfTCPSender(String host, int port, ErrorReporter errorReporter) throws IOException {
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.errorReporter = errorReporter;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown || !message.isValid()) {
            return false;
        }

        try {
            // reconnect if necessary
            if (socket == null) {
                socket = new Socket(host, port);
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

    public void close() {
        shutdown = true;
        Closer.close(socket);
    }
}
