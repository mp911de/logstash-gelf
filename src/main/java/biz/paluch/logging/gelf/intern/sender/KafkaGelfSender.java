package biz.paluch.logging.gelf.intern.sender;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * {@link GelfSender} using Kafka.
 *
 * @author Rifat Döver
 * @since 1.13
 */
public class KafkaGelfSender implements GelfSender {

    private final KafkaProducer<byte[], byte[]> kafkaProducer;
    private final String topicName;
    private final ErrorReporter errorReporter;

    public KafkaGelfSender(KafkaProducer<byte[], byte[]> kafkaProducer, String topicName, ErrorReporter errorReporter) {
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
        this.errorReporter = errorReporter;
    }

    @Override
    public boolean sendMessage(GelfMessage message) {
        ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topicName,
                message.toJson().getBytes(StandardCharsets.UTF_8));
        boolean hasOffset;
        try {
            Future<RecordMetadata> metadata = kafkaProducer.send(record);
            hasOffset = metadata.get(30, TimeUnit.SECONDS).hasOffset();
        } catch (Exception e) {
            errorReporter.reportError("Error sending log to kafka", e);
            return false;
        }
        return hasOffset;
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }
}
