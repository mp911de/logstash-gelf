package biz.paluch.logging.gelf.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaHelper;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rifat Döver
 */
@ExtendWith({ MockitoExtension.class })
public class GelfLogbackAppenderKafkaIntegrationTests {

    public static final String KAFKA_LOG_TOPIC = "log-topic";

    @Test
    public void testKafkaSender() throws Exception {
        EphemeralKafkaBroker broker = EphemeralKafkaBroker.create(19092);
        KafkaHelper helper = KafkaHelper.createFor(broker);
        broker.start();

        while (!broker.isRunning()) {
            Thread.sleep(1000);
        }

        LoggerContext lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();

        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-kafka.xml");

        configurator.doConfigure(xmlConfigFile);

        Logger testLogger = lc.getLogger("testLogger");

        testLogger.error("Log from kafka");

        KafkaConsumer<String, String> consumer = helper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);
        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);

        broker.stop();
    }
}
