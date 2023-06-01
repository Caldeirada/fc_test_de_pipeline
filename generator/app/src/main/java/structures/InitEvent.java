package structures;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InitEvent{
    @JsonProperty("event-type")
    private final String event_type = "init";
    private long time;
    @JsonProperty("user-id")
    private String user_id;
    private String country;
    private String platform;
}
