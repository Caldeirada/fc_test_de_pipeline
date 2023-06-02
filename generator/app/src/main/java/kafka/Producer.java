package kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventgenerator.EventGeneratorImpl;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import properties.PropertiesHelper;
import structures.InitEvent;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The type SimpleProducer is a wrapper class for {@link org.apache.kafka.clients.producer.KafkaProducer}.
 * The object publishes methods that send messages that have random string
 * content onto the Kafka broker defined in {@link /src/resources/config.properties}
 */

public class Producer extends AbstractKafka {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private KafkaProducer<String, String> kafkaProducer;

    /**
     * Instantiates a new Abstract class, SimpleKafka.
     * <p>
     * This abstract class's constructor provides graceful
     * shutdown behavior for Kafka producers and consumers
     *
     * @throws Exception the exception
     */
    public Producer() throws Exception {
    }


    //private final Logger log = Logger.getLogger(SimpleProducer.class.getName());

    /**
     * The runAlways method sends a message to a topic.
     *
     * @param topicName the name of topic to access
     * @throws Exception the Exception that will get thrown upon an error
     */
    public void runAlways(String topicName) throws JsonProcessingException, InterruptedException {
        while (true) {
            String key = UUID.randomUUID().toString();
            //use the Message Helper to get a random string
            EventGeneratorImpl eventGenerator = new EventGeneratorImpl();
            InitEvent initEvent = eventGenerator.generateInitEvent("2023-06-01");
            ObjectMapper mapper = new ObjectMapper();
            String initEventString = mapper.writeValueAsString(initEvent);
            //send the message
            try {
                send(topicName, key, initEventString);
            } catch (Exception e) {
                //log.error(exeption)
            }
            Thread.sleep(1000);
        }
    }

    /**
     * Does the work of sending a message to
     * a Kafka broker. The method uses the name of
     * the topic that was declared in this class's
     * constructor.
     *
     * @param topicName the name of the topic to where the message                   will be sent
     * @param key       the key value for the message
     * @param message   the content of the message
     * @throws Exception the exception that gets thrown upon error
     */
    protected void send(String topicName, String key, String message) throws Exception {
        //create the ProducerRecord object which will
        //represent the message to the Kafka broker.
        ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>(topicName, key, message);

        //Send the message to the Kafka broker using the internal
        //KafkaProducer
        getKafkaProducer().send(producerRecord);
    }

    private KafkaProducer<String, String> getKafkaProducer() throws Exception {
        if (this.kafkaProducer == null) {
            Properties props = PropertiesHelper.getProperties();
            this.kafkaProducer = new KafkaProducer<>(props);
        }
        return this.kafkaProducer;
    }

    public void shutdown() throws Exception {
        closed.set(true);
        //log.info(MessageHelper.getSimpleJSONObject("Shutting down producer"));
        getKafkaProducer().close();
    }
}
