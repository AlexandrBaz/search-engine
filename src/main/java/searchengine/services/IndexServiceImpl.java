package searchengine.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.utils.parser.Parser;
import searchengine.utils.parser.PageParser;
import searchengine.utils.parser.SiteRunnable;
import searchengine.utils.ServiceStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class IndexServiceImpl implements IndexService {
    private SitesList sitesList;
    private ExecutorService executor;
    private ServiceStore serviceStore;
    private IndexServiceAsync indexServiceAsync;
    List<Parser> parserList;

    @Override
    public Boolean startIndexing() {
//        if (!isStarted()) {
//            checkAndDeleteSiteIfPresent(sitesList);
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            parserList = new ArrayList<>();
            sitesList.getSites().forEach(site -> {
                SiteRunnable worker = new SiteRunnable(site, serviceStore, indexServiceAsync);
                Future<?> future = executor.submit(worker);
                Parser parser = new Parser();
                parser.setDomain(site.getUrl());
                parser.setFuture(future);
                parser.setWorker(worker);
                parserList.add(parser);
            });
            executor.shutdown();
            return true;
//        } else {
//            return false;
//        }
    }

    private void checkAndDeleteSiteIfPresent(@NotNull SitesList sitesList) {
        SiteRepositoryService siteRepositoryService = serviceStore.getSiteRepositoryService();
        IndexRepositoryService indexRepositoryService = serviceStore.getIndexRepositoryService();
        LemmaRepositoryService lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
        PageRepositoryService pageRepositoryService = serviceStore.getPageRepositoryService();
        sitesList.getSites().forEach(site -> {
            SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.getUrl());
            if (siteEntity != null) {
                List<Long> pageEntityListId = pageRepositoryService.getIdListPageEntity(siteEntity);
                indexRepositoryService.deleteByIdListPageEntity(pageEntityListId);
                lemmaRepositoryService.deleteByIdListPageEntity(pageEntityListId);
                pageRepositoryService.deleteByIdListPageEntity(pageEntityListId);
                siteRepositoryService.deleteSiteEntity(siteEntity);
            }
        });
    }


    @Override
    public Boolean stopIndexing() {
        if (isStarted()) {
            parserList.forEach(handler -> {
                handler.getWorker().stopForkJoin();
                if (!handler.getFuture().isDone()) {
                    SiteRepositoryService siteRepositoryService = serviceStore.getSiteRepositoryService();
                    siteRepositoryService.stopIndexingThisEntity(handler.getDomain());
                }
            });
            executor.shutdownNow();
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
    public void setIndexServiceAsync(IndexServiceAsync indexServiceAsync){this.indexServiceAsync = indexServiceAsync;}
}
