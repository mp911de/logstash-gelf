package biz.paluch.logging.gelf.log4j2;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaHelper;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rifat Döver
 */
@ExtendWith({ MockitoExtension.class })
public class GelfLogAppenderKafkaIntegrationTests {

    public static final String KAFKA_LOG_TOPIC = "kafka-log-topic";

    @Test
    public void testKafkaSender() throws Exception {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-gelf-with-kafka.xml");

        EphemeralKafkaBroker broker = EphemeralKafkaBroker.create(9092);
        KafkaHelper helper = KafkaHelper.createFor(broker);
        broker.start();

        while (!broker.isRunning()) {
            Thread.sleep(1000);
        }

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.error("Log from kafka");

        KafkaConsumer<String, String> consumer = helper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);
        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);
    }
}
