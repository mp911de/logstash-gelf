package biz.paluch.logging.gelf.intern.sender;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * {@link GelfSenderProvider} providing {@link KafkaGelfSender}.
 *
 * @author Rifat Döver
 * @since 1.13
 */
public class KafkaGelfSenderProvider implements GelfSenderProvider {

    private static final int BROKER_DEFAULT_PORT = 9092;

    @Override
    public boolean supports(String host) {
        return host != null && host.startsWith(KafkaContants.KAFKA_SCHEME + ":");
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) {

        URI uri = URI.create(configuration.getHost());
        Map<String, String> options = QueryStringParser.parse(uri);

        Properties props = new Properties();
        for (String key : options.keySet()) {
            props.setProperty(key, options.get(key));
        }

        String brokers = getBrokerServers(configuration);

        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

        String kafkaLogTopic = getTopic(uri);

        // Default Configurations
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        if (!props.containsKey(ProducerConfig.ACKS_CONFIG)) {
            props.put(ProducerConfig.ACKS_CONFIG, "all");
        } else {
            String acks = props.getProperty(ProducerConfig.ACKS_CONFIG);
            acks = "0".equalsIgnoreCase(acks) ? "1" : acks;
            props.put(ProducerConfig.ACKS_CONFIG, acks);
        }

        if (!props.containsKey(ProducerConfig.RETRIES_CONFIG)) {
            props.put(ProducerConfig.RETRIES_CONFIG, 2);
        }

        KafkaProducer<byte[], byte[]> kafkaProducer = new KafkaProducer<>(props);

        return new KafkaGelfSender(kafkaProducer, kafkaLogTopic, configuration.getErrorReporter());
    }

    private static String getBrokerServers(GelfSenderConfiguration configuration) {

        URI uri = URI.create(configuration.getHost());
        String brokers;

        if (uri.getHost() != null) {
            brokers = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : configuration.getPort() > 0 ? configuration.getPort()
                    : BROKER_DEFAULT_PORT;
            brokers += ":" + port;

        } else {
            brokers = uri.getAuthority();
        }

        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalArgumentException("Kafka URI must specify bootstrap.servers.");
        }

        return brokers;
    }

    private static String getTopic(URI uri) {

        String fragment = uri.getFragment();

        if (fragment == null || fragment.isEmpty()) {
            throw new IllegalArgumentException("Kafka URI must specify log topic as fragment.");
        }

        return fragment;
    }
}
