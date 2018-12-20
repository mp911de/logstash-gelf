package biz.paluch.logging.gelf.intern.sender;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.Future;

/**
 * Gelf sender for kafka. This sender uses Kafka.
 *
 * @author Rifat Döver
 * @since 1.13
 */
public class KafkaGelfSender implements GelfSender {
    private KafkaProducer<byte[], byte[]> kafkaProducer;
    private String topicName;
    private ErrorReporter errorReporter;

    public KafkaGelfSender(KafkaProducer<byte[], byte[]> kafkaProducer, String topicName, ErrorReporter errorReporter) {
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
        this.errorReporter = errorReporter;
    }

    @Override
    public boolean sendMessage(GelfMessage message) {
        ProducerRecord<byte[], byte[]> record = new ProducerRecord<byte[], byte[]>(topicName, message.toJson().getBytes());
        boolean hasOfset;
        try {
            Future<RecordMetadata> metadata = kafkaProducer.send(record);
            hasOfset = metadata.get().hasOffset();
        } catch (Exception e) {
            errorReporter.reportError("Error sending log to kafka", e);
            return false;
        }
        return hasOfset;
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }
}
