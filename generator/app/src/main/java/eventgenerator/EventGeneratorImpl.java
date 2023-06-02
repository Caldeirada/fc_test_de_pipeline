package eventgenerator;

import net.datafaker.Faker;
import structures.InitEvent;
import structures.MatchEvent;
import structures.PostMatchInfoEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EventGeneratorImpl extends EventGenerator {
    private static Faker faker = new Faker();

    @Override
    public InitEvent generateInitEvent(String date) {
        String start = String.format("%s 00:00:00", date);
        String end = String.format("%s 23:59:59", date);

        return InitEvent.builder()
                .country(faker.country().countryCode3())
                .platform(faker.device().platform())
                .time(faker.date().between(getTimestamp(start), getTimestamp(end)).getTime())
                .user_id(faker.idNumber().valid())
                .build();
    }

    @Override
    public MatchEvent generateMatchEvent() {
        return null;
    }

    @Override
    public PostMatchInfoEvent generatePostMatchInfoEvent() {
        return null;
    }

    private Timestamp getTimestamp(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return Timestamp.valueOf(LocalDateTime.parse(date, formatter));
    }
}
