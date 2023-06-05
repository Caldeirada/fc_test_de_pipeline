package eventgenerator;

import net.datafaker.Faker;
import structures.InAppPurchase;
import structures.InitEvent;
import structures.MatchEvent;
import structures.PostMatchInfoEvent;

import java.sql.Timestamp;
import java.util.UUID;


public class EventGeneratorImpl extends EventGenerator {

    private static final Faker faker = new Faker();
    private static final int MAX_GAME_DURATION = 10;
    private static final int MIN_GAME_TIER = 1;
    private static final int MAX_GAME_TIER = 5;

    private final Timestamp start;

    private final int timeAdvance;

    public EventGeneratorImpl(Timestamp start, int timeAdvance) {
        this.start = start;
        this.timeAdvance = timeAdvance;
    }

    @Override
    public InitEvent generateInitEvent() {
        return InitEvent.builder()
                .country(faker.country().countryCode3())
                .platform(faker.device().platform())
                .time(getRandTimestamp())
                .user_id(UUID.randomUUID().toString())
                .build();
    }

    @Override
    public MatchEvent generateMatchEvent(InitEvent user_a, InitEvent user_b) {
        return MatchEvent.builder()
                .duration((int) faker.duration().atMostMinutes(MAX_GAME_DURATION).getSeconds()) //force cast to int to overcome faker ret type
                .game_tier(faker.number().numberBetween(MIN_GAME_TIER, MAX_GAME_TIER))
                .winner(faker.expression(String.format("#{options.option '%s','%s'}", user_a.getUser_id(), user_b.getUser_id())))
                .user_a(user_a.getUser_id())
                .user_a_info(generatePostMatchInfoEvent(user_a))
                .user_b(user_b.getUser_id())
                .user_b_info(generatePostMatchInfoEvent(user_b))
                .time(getRandTimestampSinceEvent(Math.max(user_a.getTime(), user_b.getTime())))
                .build();
    }

    @Override
    public PostMatchInfoEvent generatePostMatchInfoEvent(InitEvent user) {
        return PostMatchInfoEvent.builder()
                .coins(faker.number().positive())
                .level(faker.number().positive())
                .platform(user.getPlatform())
                .device(faker.device().modelName())
                .build();
    }

    @Override
    public InAppPurchase generateIAPEvent(InitEvent user) {
        return InAppPurchase.builder()
                .user_id(user.getUser_id())
                .product_id(UUID.randomUUID().toString())
                .time(getRandTimestampSinceEvent(user.getTime()))
                .purchase_value(faker.number().positive())
                .purchase_value(faker.number().positive())
                .build();
    }

    private Long getRandTimestamp() {
        Timestamp end = new Timestamp(start.getTime());
        end.setTime(end.getTime() + timeAdvance);
        return getRandTimestamp(end);
    }

    private Long getRandTimestampSinceEvent(Long _timestamp) {
        Timestamp end = new Timestamp(_timestamp);
        end.setTime(Math.max(end.getTime(),start.getTime()) + timeAdvance);
        return getRandTimestamp(end);
    }

    private Long getRandTimestamp(Timestamp end) {
        long ts = faker.date().between(start, end).getTime();
        start.setTime(start.getTime() + timeAdvance);//advance start time to simulate a faster day
        return ts;
    }
}
