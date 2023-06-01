package structures;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostMatchInfo {
    @JsonProperty("coin-balance-after-match")
    private int coins;
    @JsonProperty("level-after-match")
    private int level;
    private String device;
    private String platform;
}
