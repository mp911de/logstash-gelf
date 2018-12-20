package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import com.github.charithe.kafka.*;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rifat Döver
 */

@ExtendWith({ KafkaJunitExtension.class, MockitoExtension.class })
@KafkaJunitExtensionConfig(startupMode = StartupMode.WAIT_FOR_STARTUP)
public class KafkaGelfSenderIntegrationTest {

    public static final String LOG_TOPIC = "test-log-topic";
    @Mock
    ErrorReporter errorReporter;
    private KafkaGelfSender kafkaGelfSender;

    @Test
    public void testSend(EphemeralKafkaBroker kafkaBroker) {
        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        KafkaHelper kafkaHelper = KafkaHelper.createFor(kafkaBroker);
        Properties props = kafkaHelper.producerConfig();
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        KafkaProducer<byte[], byte[]> byteProducer = kafkaHelper.createByteProducer(props);

        Mockito.spy(byteProducer);

        kafkaGelfSender = new KafkaGelfSender(byteProducer, LOG_TOPIC, errorReporter);

        boolean success = kafkaGelfSender.sendMessage(gelfMessage);

        KafkaConsumer<String, String> consumer = kafkaHelper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);
        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo(gelfMessage.toJson());
        assertThat(success).isTrue();

        kafkaGelfSender.close();
    }

}