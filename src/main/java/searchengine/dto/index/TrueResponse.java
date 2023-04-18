package searchengine.dto.index;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class TrueResponse extends Response {
    boolean result;
}
