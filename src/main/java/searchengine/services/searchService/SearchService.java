package searchengine.services.searchService;

import org.springframework.stereotype.Service;
import searchengine.dto.index.Response;

import java.util.Optional;

@Service
public interface SearchService {

    Response getPages(String query, String site, Integer offset, Integer limit);
}
