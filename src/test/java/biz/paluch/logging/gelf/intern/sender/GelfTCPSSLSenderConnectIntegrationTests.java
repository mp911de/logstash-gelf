package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.netty.NettyLocalServer;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Duration;
import com.google.code.tempusfugit.temporal.Timeout;
import com.google.code.tempusfugit.temporal.WaitFor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author Alexander Katanov
 * @author Mark Paluch
 */
class GelfTCPSSLSenderConnectIntegrationTests {

    private static NettyLocalServer server = new NettyLocalServer(NioServerSocketChannel.class);
    private static SSLContext sslContext;

    @BeforeAll
    static void setupClass() throws Exception {

        File file = new File("work/keystore.jks");
        assumeTrue(file.exists());

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(file), "changeit".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        final SslContext sslContext = SslContextBuilder.forServer(kmf).build();

        GelfTCPSSLSenderConnectIntegrationTests.sslContext = SSLContext.getInstance("TLSv1.2");
        GelfTCPSSLSenderConnectIntegrationTests.sslContext.init(new KeyManager[0], tmf.getTrustManagers(), null);

        server.run(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {

                ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                ch.pipeline().addLast(server.getHandler());
            }
        });
    }

    @AfterAll
    static void afterClass() {
        server.close();
    }

    @Test
    void shouldSendTCPMessagesViaSsl() throws Exception {

        final GelfTCPSSLSender tcpsslSender = new GelfTCPSSLSender("localhost", server.getPort(), 1000, 1000, 1, true,
                new ErrorReporter() {
                    @Override
                    public void reportError(String message, Exception e) {
                        System.out.println(Thread.currentThread() + " " + message);
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }, sslContext);

        final CountDownLatch started = new CountDownLatch(2);
        final CountDownLatch waitForContinue = new CountDownLatch(1);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                GelfMessage gelfMessage = new GelfMessage("short1", "long1", 1, "info");
                gelfMessage.setHost("host");
                started.countDown();
                try {
                    waitForContinue.await();
                } catch (InterruptedException e) {
                    return;
                }
                tcpsslSender.sendMessage(gelfMessage);
            }
        };

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        thread2.start();
        thread1.start();

        started.await();
        waitForContinue.countDown();

        thread1.join();
        thread2.join();

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return server.getJsonValues().size() == 2;
            }
        }, Timeout.timeout(Duration.seconds(5)));

        assertThat(server.getJsonValues()).isNotEmpty();
        assertThat(server.getJsonValues()).hasSize(2);

        tcpsslSender.close();
    }
}
