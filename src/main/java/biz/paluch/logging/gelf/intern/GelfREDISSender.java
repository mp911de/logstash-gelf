package biz.paluch.logging.gelf.intern;

import java.net.URI;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


import redis.clients.jedis.Jedis;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfREDISSender implements GelfSender {
    private boolean shutdown = false;
    private URI redisUri;
    private Jedis jedis;
    private ErrorReporter errorReporter;

    public GelfREDISSender(String redisConnStr, ErrorReporter errorReporter) throws IOException {
        this.redisUri = URI.create(redisConnStr);
        this.jedis = new Jedis(redisUri);
	//System.out.println("GelfREDISSender: New GelfREDISSender created with uri "+redisUri.toString());
        this.errorReporter = errorReporter;
    }

    public boolean sendMessage(GelfMessage message) {
        if (shutdown || !message.isValid()) {
            return false;
        }

        try {
            // reconnect if necessary
            if (jedis == null) {
		//System.out.println("GelfREDISSender.sendMessage: New GelfREDISSender created with uri "+redisUri.toString());
                jedis = new Jedis(redisUri);
            }

            //socket.getOutputStream().write(message.toTCPBuffer().array());
	    //System.out.println("GelfREDISSender.sendMessage: before sending Message "+message.toJson());
            jedis.lpush(redisUri.getFragment(),message.toJson());
	    //System.out.println("GelfREDISSender.sendMessage: after sending Message "+message.toJson());

            return true;
        } catch (Exception e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send REDIS data via URI " + redisUri.toString() , e));
            // if an error occours, signal failure
            if (jedis != null) {
                jedis.close();
            }
            jedis = null;
            return false;
        }
    }

    public void close() {
        shutdown = true;
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot close REDIS connection " , e));
        }
    }
}
