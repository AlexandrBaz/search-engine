package searchengine.services.searchservices;

import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;

@Service
public interface SearchService {

    SearchResponse getPages(String query, String site, Integer offset, Integer limit);
}
