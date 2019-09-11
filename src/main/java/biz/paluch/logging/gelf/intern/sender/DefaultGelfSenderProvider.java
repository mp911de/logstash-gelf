package biz.paluch.logging.gelf.intern.sender;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * Default provider for {@link GelfSender} that creates UDP, TCP and HTTP senders.
 *
 * @author https://github.com/Batigoal/logstash-gelf.git
 * @author Mark Paluch
 * @author Aleksandar Stojadinovic
 */
public class DefaultGelfSenderProvider implements GelfSenderProvider {

	/**
	 * Default GELF port.
	 */
    public static final int DEFAULT_PORT = 12201;

    private static final Map<String, GelfSenderProducer> factories;

    private static final GelfSenderProducer tcpSenderFactory = new GelfSenderProducer() {

        @Override
        public GelfSender create(GelfSenderConfiguration configuration, String host, int port) throws IOException {

            int defaultTimeoutMs = (int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);

            URI uri = URI.create(host);

            Map<String, String> params = QueryStringParser.parse(uri);
            int connectionTimeMs = (int) QueryStringParser.getTimeAsMs(params, GelfTCPSender.CONNECTION_TIMEOUT, defaultTimeoutMs);
            int readTimeMs = (int) QueryStringParser.getTimeAsMs(params, GelfTCPSender.READ_TIMEOUT, defaultTimeoutMs);
            int deliveryAttempts = QueryStringParser.getInt(params, GelfTCPSender.RETRIES, 1);
            boolean keepAlive = QueryStringParser.getString(params, GelfTCPSender.KEEPALIVE, false);

            String tcpGraylogHost = QueryStringParser.getHost(uri);
            SocketFactory socketFactory = SocketFactory.getDefault();

            return new GelfTCPSender(tcpGraylogHost, port, connectionTimeMs, readTimeMs, deliveryAttempts, keepAlive,
                    configuration.getErrorReporter());
        }
    };

    private static final GelfSenderProducer tcpSslSenderFactory = new GelfSenderProducer() {

        @Override
        public GelfSender create(GelfSenderConfiguration configuration, String host, int port) throws IOException {

            int defaultTimeoutMs = (int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);

            URI uri = URI.create(host);

            Map<String, String> params = QueryStringParser.parse(uri);
            int connectionTimeMs = (int) QueryStringParser.getTimeAsMs(params, GelfTCPSender.CONNECTION_TIMEOUT, defaultTimeoutMs);
            int readTimeMs = (int) QueryStringParser.getTimeAsMs(params, GelfTCPSender.READ_TIMEOUT, defaultTimeoutMs);
            int deliveryAttempts = QueryStringParser.getInt(params, GelfTCPSender.RETRIES, 1);
            boolean keepAlive = QueryStringParser.getString(params, GelfTCPSender.KEEPALIVE, false);

            String tcpGraylogHost = QueryStringParser.getHost(uri);

            try {
                return new GelfTCPSSLSender(tcpGraylogHost, port, connectionTimeMs, readTimeMs, deliveryAttempts, keepAlive,
                        configuration.getErrorReporter(), SSLContext.getDefault());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    private static final GelfSenderProducer udpSenderFactory = new GelfSenderProducer() {

        @Override
        public GelfSender create(GelfSenderConfiguration configuration, String host, int port) throws IOException {

            URI uri = URI.create(host);
            String udpGraylogHost = QueryStringParser.getHost(uri);
            return new GelfUDPSender(udpGraylogHost, port, configuration.getErrorReporter());
        }
    };

    private static final GelfSenderProducer defaultSenderFactory = new GelfSenderProducer() {

        @Override
        public GelfSender create(GelfSenderConfiguration configuration, String host, int port) throws IOException {
            return new GelfUDPSender(host, port, configuration.getErrorReporter());
        }
    };

    private static final GelfSenderProducer httpSenderFactory = new GelfSenderProducer() {

        @Override
        public GelfSender create(GelfSenderConfiguration configuration, String host, int port) throws IOException {

            int defaultTimeoutMs = (int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);
            URL url = new URL(host);

            return new GelfHTTPSender(url, defaultTimeoutMs, defaultTimeoutMs, configuration.getErrorReporter());
        }
    };

    static {

        Map<String, GelfSenderProducer> prefixToFactory = new HashMap<>();
        prefixToFactory.put("tcp:", tcpSenderFactory);
        prefixToFactory.put("ssl:", tcpSslSenderFactory);
        prefixToFactory.put("udp:", udpSenderFactory);
        prefixToFactory.put("http", httpSenderFactory);

        factories = Collections.unmodifiableMap(prefixToFactory);

    }

    @Override
    public boolean supports(String host) {
        return host != null;
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {

        String graylogHost = configuration.getHost();

        int port = configuration.getPort();
        if (port == 0) {
            port = DEFAULT_PORT;
        }

        for (Map.Entry<String, GelfSenderProducer> entry : factories.entrySet()) {
            if (graylogHost.startsWith(entry.getKey())) {
                return entry.getValue().create(configuration, graylogHost, port);
            }
        }

        return defaultSenderFactory.create(configuration, graylogHost, port);

    }

    private interface GelfSenderProducer {

        /**
         * Produce a {@link GelfSender} using {@code configuration}, {@code host} and {@code port},
         *
         * @param configuration
         * @param host
         * @param port
         * @return
         * @throws IOException
         */
        GelfSender create(GelfSenderConfiguration configuration, String host, int port) throws IOException;
    }
}
