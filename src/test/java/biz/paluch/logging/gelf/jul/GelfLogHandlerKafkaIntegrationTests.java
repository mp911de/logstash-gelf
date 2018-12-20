package biz.paluch.logging.gelf.jul;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaHelper;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

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
        broker.start();

        while (!broker.isRunning()) {
            Thread.sleep(1000);
        }

        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-kafka-logging.properties"));

        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, "Log from kafka");

        KafkaConsumer<String, String> consumer = helper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);
        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);
    }
}
