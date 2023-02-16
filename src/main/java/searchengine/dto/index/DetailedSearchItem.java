package searchengine.dto.index;

import lombok.Data;

@Data
public class DetailedSearchItem {
    private String site;
    private String siteName;
    private String url;
    private String title;
    private String snippet;
    private float relevance;

}
