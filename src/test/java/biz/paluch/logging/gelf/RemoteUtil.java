package biz.paluch.logging.gelf;

import java.io.IOException;
import java.net.ServerSocket;

public class RemoteUtil {

    public static int findFreePort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }
}
