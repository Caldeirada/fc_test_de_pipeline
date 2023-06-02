package eventgenerator;

import net.datafaker.Faker;
import structures.InAppPurchase;
import structures.InitEvent;
import structures.MatchEvent;
import structures.PostMatchInfoEvent;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class EventGeneratorImpl extends EventGenerator {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Faker faker = new Faker();
    private static final int MAX_GAME_DURATION = 10;
    private static final int MIN_GAME_TIER = 1;
    private static final int MAX_GAME_TIER = 5;

    @Override
    public InitEvent generateInitEvent(String date) {
        return InitEvent.builder()
                .country(faker.country().countryCode3())
                .platform(faker.device().platform())
                .time(getRandTimestamp(date))
                .user_id(UUID.randomUUID().toString())
                .build();
    }

    @Override
    public MatchEvent generateMatchEvent(String date, InitEvent user_a, InitEvent user_b) {
        return MatchEvent.builder()
                .duration((int) faker.duration().atMostMinutes(MAX_GAME_DURATION).getSeconds()) //force cast to int to overcome faker ret type
                .game_tier(faker.number().numberBetween(MIN_GAME_TIER, MAX_GAME_TIER))
                .winner(faker.expression(String.format("#{options.option '%s','%s'}", user_a.getUser_id(), user_b.getUser_id())))
                .user_a(user_a.getUser_id())
                .user_a_info(generatePostMatchInfoEvent(user_a))
                .user_b(user_b.getUser_id())
                .user_b_info(generatePostMatchInfoEvent(user_b))
                .time(getRandTimestampSinceEvent(date, Math.max(user_a.getTime(), user_b.getTime())))
                .build();
    }

    @Override
    public PostMatchInfoEvent generatePostMatchInfoEvent(InitEvent user) {
        return PostMatchInfoEvent.builder()
                .coins(faker.number().positive())
                .level(faker.number().positive())
                .platform(user.getPlatform())
                .device(faker.device().toString())
                .build();
    }

    @Override
    public InAppPurchase generateIAPEvent(String date, InitEvent user) {
        return InAppPurchase.builder()
                .user_id(user.getUser_id())
                .product_id(UUID.randomUUID().toString())
                .time(getRandTimestampSinceEvent(date, user.getTime()))
                .purchase_value(faker.number().positive())
                .purchase_value(faker.number().positive())
                .build();
    }

    private Timestamp getTimestamp(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault());
        return Timestamp.valueOf(LocalDateTime.parse(date, formatter));
    }

    private Long getRandTimestamp(String date) {
        String start = String.format("%s 00:00:00", date);
        String end = String.format("%s 23:59:59", date);
        return getRandTimestamp(getTimestamp(start), getTimestamp(end));
    }

    private Long getRandTimestampSinceEvent(String date, Long _timestamp) {
        Timestamp start = new Timestamp(_timestamp);
        String end = String.format("%s 23:59:59", date);
        return getRandTimestamp(start, getTimestamp(end));
    }

    private Long getRandTimestamp(Timestamp start, Timestamp end) {
        return faker.date().between(start, end).getTime();
    }
}
