package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.model.IndexEntity;

import java.util.List;

@Service
public interface IndexServiceAsync {
    void parsePage(String url, Site site);

    void writeIndexRank(List<IndexEntity> indexEntityList);
}
