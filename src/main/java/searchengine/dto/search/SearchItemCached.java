package searchengine.dto.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SearchItemCached {
    long pageId;
    float absoluteRelevance;
    float relevance;
}
