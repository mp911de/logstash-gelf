package biz.paluch.logging.gelf.intern.sender;

import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.sun.tools.javac.util.StringUtils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.message.DeleteAclsRequestData.DeleteAclsFilter;
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
        for (Map.Entry<String, String> entry : options.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
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

        // extract the host part from uri and put in an array
        // so each host can be validated using the URI

        // from https://docs.oracle.com/javase/7/docs/api/java/net/URI.html
        // A hierarchical URI is subject to further parsing according to the syntax
        // [scheme:][//authority][path][?query][#fragment]
        String hconf = configuration.getHost();

        // in order to arrive here, there have to be a kafka scheme
        // get the scheme part
        String scheme = URI.create(hconf).getScheme()+"://";
        // and then begining of host
        String hostsPart = hconf.substring( scheme.length() );

        // hostsPart ends with either # or ?
        int pos;
        for(pos=0; pos<hostsPart.length(); pos++) {
            switch(hostsPart.charAt(pos)) {
                case '#':
                case '?':
                    break;
                default:
                    continue;
            }
            break;
        }
        String suffix=hostsPart.substring(pos);
        hostsPart=hostsPart.substring(0, pos);

        String brokers = "";
        String[] hosts = new String[0];
        if (hostsPart.length()>0)
            hosts = hostsPart.split(",");
        if (hosts.length>0) for(String host: hosts) {
            String broker;
            String tmp = scheme + host + suffix;
            URI uri = URI.create(tmp);
            if (uri.getHost() != null) {
                broker = uri.getHost();
                int port;
                if (uri.getPort() > 0) {
                    port = uri.getPort();
                } else if (configuration.getPort() > 0) {
                    port = configuration.getPort();
                } else {
                    port = BROKER_DEFAULT_PORT;
                }
                broker += ":" + port;

            } else {
                broker = uri.getAuthority();
            }
            if (brokers.length()>0)
                brokers += ",";
            brokers += broker;
        }
        if (brokers.isEmpty()) {
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
