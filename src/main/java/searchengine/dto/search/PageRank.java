package searchengine.dto.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class PageRank {
    private String domain;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float absRelevance;
    private float relevance;
}
