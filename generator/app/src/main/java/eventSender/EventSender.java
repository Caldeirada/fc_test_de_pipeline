package eventSender;

import com.fasterxml.jackson.core.JsonProcessingException;
import eventgenerator.EventGenerator;
import kafka.KafkaProducer;

/**
 * Class to handle the event creation and publish to queue
 * EventGenerator creates the
 */

public abstract class EventSender {
    /**
     * Starts the process of creating and publishing simulated events to the queue
     */
    public abstract void startProcess() throws InterruptedException, JsonProcessingException;
}
