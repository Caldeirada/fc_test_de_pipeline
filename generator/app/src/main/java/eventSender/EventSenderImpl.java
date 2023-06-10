package eventSender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eventgenerator.EventGenerator;
import kafka.KafkaProducer;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import structures.InAppPurchase;
import structures.InitEvent;
import structures.MatchEvent;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class EventSenderImpl extends EventSender {
    private final Logger log = LogManager.getLogger(EventSender.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();

    private KafkaProducer kafkaProducer;

    private EventGenerator eventGenerator;

    // cache with ttl to provide more realistic events
    private Cache<String, InitEvent> initEventCache;

    private Random rand;

    public EventSenderImpl(KafkaProducer kafkaProducer, EventGenerator eventGenerator) {
        this.kafkaProducer = kafkaProducer;
        this.eventGenerator = eventGenerator;
        this.initEventCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        this.rand = new Random();
    }

    @Override
    public void startProcess() throws InterruptedException, JsonProcessingException {
        int chance;
        while (true) {
            chance = rand.nextInt(101);
            //90% of the loops will create a new init event
            if (chance < 90) {
                createAndPublishInitEvent();
            }

            // 70% of the loop will create a new match event
            if (chance < 70) {
                createAndPublishMatchEvent();
            }

            // 30% of the loops will create a new iap event
            if (chance < 30) {
                createAndPublishIAPEvent();
            }

            Thread.sleep(100);
        }
    }

    private void send(String key, String value) {
        try {
            kafkaProducer.send(key, value);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    private void createAndPublishInitEvent() throws JsonProcessingException {
        InitEvent initEvent = eventGenerator.generateInitEvent();
        String key = String.format("%s_%s", initEvent.getEvent_type(), UUID.randomUUID());
        initEventCache.put(key, initEvent);
        send(key, mapper.writeValueAsString(initEvent));
    }

    private void createAndPublishMatchEvent() throws JsonProcessingException {
        InitEvent user_a = getCachedInitEvent();
        InitEvent user_b = getCachedInitEvent();
        if (!user_a.getUser_id().equals(user_b.getUser_id())) {
            MatchEvent matchEvent = eventGenerator.generateMatchEvent(user_a, user_b);
            String key = String.format("%s_%s", matchEvent.getEvent_type(), UUID.randomUUID());
            send(key, mapper.writeValueAsString(matchEvent));
        }
    }

    private void createAndPublishIAPEvent() throws JsonProcessingException {
        InitEvent user = getCachedInitEvent();
        InAppPurchase iapEvent = eventGenerator.generateIAPEvent(user);
        String key = String.format("%s_%s", iapEvent.getEvent_type(), UUID.randomUUID());
        send(key, mapper.writeValueAsString(iapEvent));
    }

    /**
     * Used to fetch generated init events currently in cache
     */
    private InitEvent getCachedInitEvent() {

        ArrayList<String> eventsKeys = new ArrayList<>(initEventCache.asMap().keySet());

        return initEventCache.getIfPresent(eventsKeys.get(rand.nextInt(eventsKeys.size())));

    }
}
