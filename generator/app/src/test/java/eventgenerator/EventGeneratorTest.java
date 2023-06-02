package eventgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;
import structures.InitEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EventGeneratorTest {
    public static final String INIT_JSON = "init.json";

    @Test
    public void gen_init_event_correct() {
        EventGeneratorImpl eventGenerator = new EventGeneratorImpl();

        InitEvent initEvent = eventGenerator.generateInitEvent("2023-06-02");
        assertNotNull(initEvent.getEvent_type());
        assertNotNull(initEvent.getCountry());
        assertNotNull(initEvent.getPlatform());
        assertNotNull(initEvent.getUser_id());
        test_event(initEvent, INIT_JSON);
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