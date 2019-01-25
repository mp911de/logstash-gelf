package biz.paluch.logging.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaHelper;
import com.google.common.collect.Lists;

/**
 * @author Rifat Döver
 */
@ExtendWith({ MockitoExtension.class })
public class GelfLogHandlerKafkaIntegrationTests {

    public static final String KAFKA_LOG_TOPIC = "kafka-log-topic";

    @Test
    public void testKafkaSender() throws Exception {

        EphemeralKafkaBroker broker = EphemeralKafkaBroker.create(19092);
        KafkaHelper helper = KafkaHelper.createFor(broker);
        broker.start().get(30, TimeUnit.SECONDS);

        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-kafka-logging.properties"));

        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, "Log from kafka");

        KafkaConsumer<String, String> consumer = helper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);
        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);

        broker.stop();
    }
}
