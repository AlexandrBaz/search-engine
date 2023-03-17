package searchengine.services;

import searchengine.dto.index.IndexResponse;
import searchengine.dto.index.TrueResponse;

public interface StartIndexingService {
    IndexResponse startIndexing();
    Boolean stopIndexing();
    Boolean indexPage();

}
