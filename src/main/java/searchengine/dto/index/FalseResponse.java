package searchengine.dto.index;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class FalseResponse extends Response {
    boolean result;
    String error;
}
