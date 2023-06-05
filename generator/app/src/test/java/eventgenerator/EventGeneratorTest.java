package eventgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;
import structures.InAppPurchase;
import structures.InitEvent;
import structures.MatchEvent;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.*;

public class EventGeneratorTest {
    private static final String INIT_JSON = "init.json";
    private static final String MATCH_JSON = "match.json";
    private static final String IAP_JSON = "in-app-purchase.json";
    private static final  String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int timeAdvance = 36000;

    @Test
    public void gen_init_event_correct() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault());
        Timestamp ts = Timestamp.valueOf(LocalDateTime.parse("2023-06-01 00:00:00", formatter));
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl(ts, timeAdvance);

        InitEvent initEvent = eventGenerator.generateInitEvent();
        assertNotNull(initEvent.getEvent_type());
        assertEquals(initEvent.getEvent_type(), "init");
        assertNotNull(initEvent.getCountry());
        assertNotNull(initEvent.getPlatform());
        assertNotNull(initEvent.getUser_id());
        test_event(initEvent, INIT_JSON);
    }

    @Test
    public void gen_match_event_correct() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault());
        Timestamp ts = Timestamp.valueOf(LocalDateTime.parse("2023-06-01 00:00:00", formatter));
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl(ts, timeAdvance);

        InitEvent user_a = eventGenerator.generateInitEvent();
        InitEvent user_b = eventGenerator.generateInitEvent();
        MatchEvent matchEvent = eventGenerator.generateMatchEvent(user_a, user_b);

        assertNotNull(matchEvent.getEvent_type());
        assertEquals(matchEvent.getEvent_type(), "match");
        assertNotNull(matchEvent.getUser_a());
        assertEquals(matchEvent.getUser_a(), user_a.getUser_id());
        assertNotNull(matchEvent.getUser_b());
        assertEquals(matchEvent.getUser_b(), user_b.getUser_id());
        assertNotNull(matchEvent.getUser_a_info());
        assertEquals(matchEvent.getUser_a_info().getPlatform(), user_a.getPlatform());
        assertNotNull(matchEvent.getUser_b_info());
        assertEquals(matchEvent.getUser_b_info().getPlatform(), user_b.getPlatform());

        // ensure event is after user init event
        assertTrue(matchEvent.getTime() > user_a.getTime());
        assertTrue(matchEvent.getTime() > user_b.getTime());
        test_event(matchEvent, MATCH_JSON);
    }

    @Test
    public void gen_IAP_event_correct() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault());
        Timestamp ts = Timestamp.valueOf(LocalDateTime.parse("2023-06-01 00:00:00", formatter));
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl(ts, timeAdvance);

        InitEvent user = eventGenerator.generateInitEvent();
        InAppPurchase inAppPurchase = eventGenerator.generateIAPEvent(user);

        assertNotNull(inAppPurchase.getEvent_type());
        assertEquals(inAppPurchase.getEvent_type(), "in-app-purchase");
        assertNotNull(inAppPurchase.getUser_id());
        assertTrue(inAppPurchase.getPurchase_value() >= 0);

        // ensure event is after user init event
        assertTrue(inAppPurchase.getTime() > user.getTime());
        test_event(inAppPurchase, IAP_JSON);
    }

    private <T> void test_event(T event, String schema) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(event);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        try (InputStream schemaStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(schema)) {
            JsonSchema jsonSchema = factory.getSchema(schemaStream);
            Set<ValidationMessage> validationResult = jsonSchema.validate(jsonNode);

            assertTrue(validationResult.isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}