package kafka;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The type Abstract class SimpleKafka
 */
public abstract class AbstractKafka {
    /**
     * Instantiates a new Abstract class, SimpleKafka.
     * <p>
     * This abstract class's constructor provides graceful
     * shutdown behavior for Kafka producers and consumers
     *
     * @throws Exception the exception
     */
    public AbstractKafka() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //log.info(MessageHelper.getSimpleJSONObject("Created the Shutdown Hook"));
    }
    //private final Logger log = Logger.getLogger(AbstractSimpleKafka.class.getName());

    /**
     * The inherited classes will provide the behavior necessary
     * to shut down gracefully.
     *
     * @throws Exception the exception that get thrown upon error
     */
    public abstract void shutdown() throws Exception;

    /**
     * This purpose of this method is to provide continuous
     * behavior to produce or consume messages from a Kafka
     * broker
     *
     * @param topicName    the topicName to execute against
     */
    public abstract void runAlways(String topicName) throws JsonProcessingException, InterruptedException;
}