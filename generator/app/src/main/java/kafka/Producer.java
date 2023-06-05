package kafka;

import lombok.NonNull;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import properties.PropertiesHelper;

import java.util.Properties;


/**
 * The type SimpleProducer is a wrapper class for {@link org.apache.kafka.clients.producer.KafkaProducer}.
 * The object publishes methods that send messages that have random string
 * content onto the Kafka broker defined in {@link /src/resources/config.properties}
 */

public class Producer extends KafkaProducer {
    private final Logger log = LogManager.getLogger(Producer.class.getName());
    private String topicName;
    @NonNull
    private org.apache.kafka.clients.producer.KafkaProducer<String, String> kafkaProducer;

    public Producer(String topicName) {
        this.topicName = topicName;
        kafkaProducer = getKafkaProducer();
    }

    /**
     * Does the work of sending a message to
     * a Kafka broker. The method uses the name of
     * the topic that was declared in this class's
     * constructor.
     *
     * @param key     the key value for the message
     * @param message the content of the message
     * @throws Exception the exception that gets thrown upon error
     */
    public void send(String key, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, message);

        try {
            getKafkaProducer().send(producerRecord);
        } catch (Exception e) {
            log.error(String.format("[Topic Name: %s]Failed to publish message", topicName), e);
        }
    }

    private org.apache.kafka.clients.producer.KafkaProducer<String, String> getKafkaProducer() {
        if (kafkaProducer == null) {
            Properties props = PropertiesHelper.getProperties();
            kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        }
        return kafkaProducer;
    }
}
