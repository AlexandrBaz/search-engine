package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchItem {
    private String siteName;
    private String domain;
    private String uri;
    private String title;
    private String snippet;
    private float absoluteRelevance;
    private float relevance;
}
