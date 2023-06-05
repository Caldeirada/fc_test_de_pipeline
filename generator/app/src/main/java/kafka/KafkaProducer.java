package kafka;

/**
 * The type Abstract class SimpleKafka
 */
public abstract class KafkaProducer {

    public abstract void send(String key, String message);
}