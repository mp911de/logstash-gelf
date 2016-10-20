package biz.paluch.logging.gelf.intern.sender;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.logging.gelf.NettyLocalServer;
import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class GelfTCPSSLSenderTest {

    private static NettyLocalServer server = new NettyLocalServer(NioServerSocketChannel.class);
    private static SSLContext sslContext;

    @BeforeClass
    public static void setupClass() throws Exception {

        File file = new File("work/keystore.jks");
        assumeTrue(file.exists());

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(file), "changeit".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        final SslContext sslContext = SslContextBuilder.forServer(kmf).build();

        GelfTCPSSLSenderTest.sslContext = SSLContext.getInstance("TLSv1");
        GelfTCPSSLSenderTest.sslContext.init(new KeyManager[0], tmf.getTrustManagers(), null);

        server.run(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {

                ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                ch.pipeline().addLast(server.getHandler());
            }
        });
    }

    @Test
    public void shouldSendTCPMessagesViaSsl() throws Exception {

        GelfTCPSSLSender tcpsslSender = new GelfTCPSSLSender("localhost", server.getPort(), 1000, 1000, 1, true,
                new ErrorReporter() {
                    @Override
                    public void reportError(String message, Exception e) {
                        System.out.println(message);
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }, sslContext);

        tcpsslSender.connect();

        GelfMessage gelfMessage = new GelfMessage("hello", "world", 1234, "7");
        tcpsslSender.write(gelfMessage.toTCPBuffer());
        for (int i = 0; i < 100; i++) {
            if (!server.getJsonValues().isEmpty()) {
                continue;

            }
            Thread.sleep(100);
        }

        assertThat(server.getJsonValues()).isNotEmpty();

        tcpsslSender.close();

    }

    @AfterClass
    public static void afterClass() throws Exception {

        if (server != null) {
            server.close();
        }
    }

}
