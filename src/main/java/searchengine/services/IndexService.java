package searchengine.services;

import org.springframework.stereotype.Service;

@Service
public interface IndexService {
    Boolean startIndexing();
    Boolean stopIndexing();
    Boolean indexPage(String url);

}
