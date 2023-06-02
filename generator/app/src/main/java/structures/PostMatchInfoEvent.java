package structures;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostMatchInfoEvent {
    @JsonProperty("coin-balance-after-match")
    private int coins;
    @JsonProperty("level-after-match")
    private int level;
    private String device;
    private String platform;
}
