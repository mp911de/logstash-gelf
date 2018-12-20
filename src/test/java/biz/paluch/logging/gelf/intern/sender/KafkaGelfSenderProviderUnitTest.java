package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Rifat Döver
 */
@ExtendWith(MockitoExtension.class)
class KafkaGelfSenderProviderUnitTest {
    @Mock
    GelfSenderConfiguration configuration;

    private KafkaGelfSenderProvider kafkaSenderProvider = new KafkaGelfSenderProvider();

    @Test
    public void testSupports() {
        assertFalse(kafkaSenderProvider.supports(null));
        assertFalse(kafkaSenderProvider.supports(""));
        assertFalse(kafkaSenderProvider.supports("redis:"));
        assertFalse(kafkaSenderProvider.supports("http:"));
        assertFalse(kafkaSenderProvider.supports("tcp:"));
        assertFalse(kafkaSenderProvider.supports("udp:"));
        assertFalse(kafkaSenderProvider.supports("kafka"));
        assertTrue(kafkaSenderProvider.supports("kafka:"));
    }

    @Test
    void testValidUri() throws IOException {
        String host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#kafka-log-topic";
        when(configuration.getHost()).thenReturn(host);
        assertNotNull(kafkaSenderProvider.create(configuration));

    }

    @Test
    void testUnspecifiedTopic() throws IOException {
        String host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#";
        when(configuration.getHost()).thenReturn(host);
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                kafkaSenderProvider.create(configuration);
            }
        }, "Kafka URI must specify log topic as fragment.");

        host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2";
        when(configuration.getHost()).thenReturn(host);
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                kafkaSenderProvider.create(configuration);
            }
        }, "Kafka URI must specify log topic as fragment.");
    }

    @Test
    void testUnspecifiedBroker() throws IOException {
        String host = "kafka://?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#";
        when(configuration.getHost()).thenReturn(host);
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                kafkaSenderProvider.create(configuration);
            }
        }, "Kafka URI must specify bootstrap.servers.");

    }

    @Test
    void testValidPortNotSpecified() throws IOException {
        String host = "kafka://localhost#topic";
        when(configuration.getHost()).thenReturn(host);
        assertNotNull(kafkaSenderProvider.create(configuration));
    }

    @Test
    void testValidPortSpecifiedInConfig() throws IOException {
        String host = "kafka://localhost#topic";
        when(configuration.getHost()).thenReturn(host);
        when(configuration.getPort()).thenReturn(9091);
        assertNotNull(kafkaSenderProvider.create(configuration));
    }

}