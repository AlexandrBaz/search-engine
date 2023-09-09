package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;

@Service
public interface IndexServiceAsync {
    void parsePage(String url, Site site);
}
