package searchengine.dto.search;

import lombok.Data;

@Data
public class SnippetRank {
    String snippet;
    int count;
}
