package searchengine.services.indexServices;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.services.indexServices.parser.Parser;
import searchengine.services.repoServices.ServiceStore;
import searchengine.services.repoServices.SiteRepositoryService;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {
    private SitesList sitesList;
    private ServiceStore serviceStore;
    private IndexServiceAsync indexServiceAsync;


    @Override
    public Boolean startIndexing() {
        if (!isStarted()) {
            indexServiceAsync.startIndexing();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean stopIndexing() {
        List<Parser> parserList = indexServiceAsync.getParserList();
        if (isStarted()) {
            parserList.forEach(handler -> {
                handler.getWorker().stopForkJoin();
                if (!handler.getFuture().isDone()) {
                    SiteRepositoryService siteRepositoryService = serviceStore.getSiteRepositoryService();
                    siteRepositoryService.stopIndexingThisEntity(handler.getDomain());
                }
            });
            indexServiceAsync.getExecutor().shutdownNow();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean indexPage(@NotNull String url) {
        Site site = sitesList.getSites().stream()
                .filter(site1 -> url.contains(site1.getUrl()))
                .findFirst()
                .orElse(null);
        if (site != null) {
            indexServiceAsync.parsePage(url, site);
            return true;
        } else {
            return false;
        }
    }

    private boolean isStarted() {
        List<Parser> parserList = indexServiceAsync.getParserList();
        if (parserList != null) {
            for (Parser parser : parserList) {
                if (!parser.getFuture().isDone()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Autowired
    public void setSitesList(SitesList sitesList) {
        this.sitesList = sitesList;
    }

    @Autowired
    public void setServiceStore(ServiceStore serviceStore) {
        this.serviceStore = serviceStore;
    }

    @Autowired
    public void setIndexServiceAsync(IndexServiceAsync indexServiceAsync) {
        this.indexServiceAsync = indexServiceAsync;
    }
}