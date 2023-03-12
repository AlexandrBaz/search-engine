package searchengine.services;

import searchengine.dto.index.IndexResponse;
import searchengine.dto.index.TrueResponse;

public interface StartIndexingService {
    IndexResponse getStart();
    TrueResponse stopIndexing();
    Boolean indexPage();

}
