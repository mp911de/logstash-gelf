package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;

import com.github.charithe.kafka.KafkaHelper;
import com.github.charithe.kafka.KafkaJunitExtension;
import com.github.charithe.kafka.KafkaJunitExtensionConfig;
import com.github.charithe.kafka.StartupMode;

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
    void testKafkaGelfSenderProvider(KafkaHelper helper) {

        StringBuilder builder = new StringBuilder();
        builder.append("kafka://");
        builder.append(helper.producerConfig().getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        builder.append("?request.timeout.ms=50&acks=all&client.id=kafka-junit&batch.size=10");
        builder.append("#");
        builder.append(TEST_LOG_TOPIC);

        when(gelfSenderConfiguration.getHost()).thenReturn(builder.toString());
        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        KafkaGelfSenderProvider provider = new KafkaGelfSenderProvider();
        GelfSender sender = provider.create(gelfSenderConfiguration);
        assertThat(sender).isNotNull();
        sender.close();
    }
}
