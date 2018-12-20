package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import com.github.charithe.kafka.KafkaHelper;
import com.github.charithe.kafka.KafkaJunitExtension;
import com.github.charithe.kafka.KafkaJunitExtensionConfig;
import com.github.charithe.kafka.StartupMode;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Rifat Döver
 * @since 1.13
 */
@ExtendWith({ KafkaJunitExtension.class, MockitoExtension.class })
@KafkaJunitExtensionConfig(startupMode = StartupMode.WAIT_FOR_STARTUP)
class KafkaGelfSenderProviderIntegrationTest {
    private static final String TEST_LOG_TOPIC = "log-topic";
    @Mock
    GelfSenderConfiguration gelfSenderConfiguration;

    @Test
    public void testKafkaGelfSenderProvider(KafkaHelper helper) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("kafka://");
        builder.append(helper.producerConfig().getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        builder.append("?");
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/kafka/producer.properties"));
        for (Object key : properties.keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(properties.get(key));
            builder.append("&");
        }
        builder.append("#");
        builder.append(TEST_LOG_TOPIC);
        when(gelfSenderConfiguration.getHost()).thenReturn(builder.toString());
        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        KafkaGelfSenderProvider provider = new KafkaGelfSenderProvider();
        GelfSender sender = provider.create(gelfSenderConfiguration);
        assertNotNull(sender);

        boolean success = sender.sendMessage(gelfMessage);
        assertThat(success).isTrue();

        KafkaConsumer<String, String> consumer = helper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(TEST_LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);

        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo(gelfMessage.toJson());

        sender.close();
    }

}