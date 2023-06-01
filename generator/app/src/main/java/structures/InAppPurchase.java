package structures;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InAppPurchase{
    @JsonProperty("event-type")
    private final String event_type = "in-app-purchase";
    private long time;
    private int purchase_value;
    @JsonProperty("user-id")
    private String user_id;
    @JsonProperty("product-id")
    private String product_id;
}
