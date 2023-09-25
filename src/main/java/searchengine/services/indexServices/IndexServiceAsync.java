package searchengine.services.indexServices;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.services.indexServices.parser.Parser;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public interface IndexServiceAsync {
    void parsePage(String url, Site site);
    void startIndexing();
    List<Parser> getParserList();
    ExecutorService getExecutor();
}
