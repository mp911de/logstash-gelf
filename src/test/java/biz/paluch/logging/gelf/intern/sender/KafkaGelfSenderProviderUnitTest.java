package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;

/**
 * @author Rifat Döver
 */
@ExtendWith(MockitoExtension.class)
class KafkaGelfSenderProviderUnitTest {

    @Mock
    GelfSenderConfiguration configuration;

    private KafkaGelfSenderProvider kafkaSenderProvider = new KafkaGelfSenderProvider();

    @Test
    void testSupports() {

        List<String> prefixes = Arrays.asList(null, "", "tcp", "kafka");

        for (String prefix : prefixes) {
            assertThat(kafkaSenderProvider.supports(prefix)).as("Prefix '" + prefix + "'").isFalse();
        }

        assertThat(kafkaSenderProvider.supports("kafka:")).isTrue();
    }

    @Test
    void testValidUri() {

        String host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#kafka-log-topic";
        when(configuration.getHost()).thenReturn(host);
        assertThat(kafkaSenderProvider.create(configuration)).isNotNull();
    }

    @Test
    void testUnspecifiedTopic() {

        String host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#";
        when(configuration.getHost()).thenReturn(host);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                kafkaSenderProvider.create(configuration);
            }
        }).hasMessage("Kafka URI must specify log topic as fragment.");

        host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2";
        when(configuration.getHost()).thenReturn(host);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                kafkaSenderProvider.create(configuration);
            }
        }).hasMessage("Kafka URI must specify log topic as fragment.");
    }

    @Test
    void testUnspecifiedBroker() {

        String host = "kafka://?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#";
        when(configuration.getHost()).thenReturn(host);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                kafkaSenderProvider.create(configuration);
            }
        }).hasMessage("Kafka URI must specify bootstrap.servers.");
    }

    @Test
    void testValidPortNotSpecified() {
        String host = "kafka://localhost#topic";
        when(configuration.getHost()).thenReturn(host);
        assertThat(kafkaSenderProvider.create(configuration)).isNotNull();
    }

    @Test
    void testValidPortSpecifiedInConfig() {
        String host = "kafka://localhost#topic";
        when(configuration.getHost()).thenReturn(host);
        when(configuration.getPort()).thenReturn(9091);
        assertThat(kafkaSenderProvider.create(configuration)).isNotNull();
    }
}
