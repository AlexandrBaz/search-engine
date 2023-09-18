package searchengine.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    boolean result;
    int count;
    List<SearchItem> data;

}
