package structures;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchEvent {
    @JsonProperty("event-type")
    private final String event_type = "match";
    private long time;
    @JsonProperty("user-a")
    private String user_a;
    @JsonProperty("user-b")
    private String user_b;
    @JsonProperty("user-a-postmatch-info")
    private PostMatchInfoEvent user_a_info;
    @JsonProperty("user-b-postmatch-info")
    private PostMatchInfoEvent user_b_info;
    private String winner;
    @JsonProperty("game-tier")
    private int game_tier;
    private int duration;
}
