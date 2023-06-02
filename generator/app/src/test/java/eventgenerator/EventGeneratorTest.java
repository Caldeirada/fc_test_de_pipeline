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
import java.util.Set;

import static org.junit.Assert.*;

public class EventGeneratorTest {
    public static final String INIT_JSON = "init.json";
    public static final String MATCH_JSON = "match.json";
    public static final String IAP_JSON = "in-app-purchase.json";

    @Test
    public void gen_init_event_correct() {
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl();

        InitEvent initEvent = eventGenerator.generateInitEvent("2023-06-02");
        assertNotNull(initEvent.getEvent_type());
        assertEquals(initEvent.getEvent_type(), "init");
        assertNotNull(initEvent.getCountry());
        assertNotNull(initEvent.getPlatform());
        assertNotNull(initEvent.getUser_id());
        test_event(initEvent, INIT_JSON);
    }

    @Test
    public void gen_match_event_correct() {
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl();
        InitEvent user_a = eventGenerator.generateInitEvent("2023-06-02");
        InitEvent user_b = eventGenerator.generateInitEvent("2023-06-02");
        MatchEvent matchEvent = eventGenerator.generateMatchEvent("2023-06-02", user_a, user_b);
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
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl();
        InitEvent user = eventGenerator.generateInitEvent("2023-06-02");

        InAppPurchase inAppPurchase = eventGenerator.generateIAPEvent("2023-06-02", user);
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