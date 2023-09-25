package searchengine.services.indexServices;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.indexServices.parser.PageParser;
import searchengine.services.indexServices.parser.Parser;
import searchengine.services.indexServices.parser.SiteRunnable;
import searchengine.services.repoServices.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class IndexServiceAsyncImpl implements IndexServiceAsync {
    private ServiceStore serviceStore;
    private ExecutorService executor;
    private List<Parser> parserList;
    private final SitesList sitesList;

    public IndexServiceAsyncImpl(SitesList sitesList) {
        this.sitesList = sitesList;
    }
    @Override
    @Async
    public void parsePage(String url, @NotNull Site site) {
        System.out.println(serviceStore);
        SiteRepositoryService siteRepositoryService = serviceStore.getSiteRepositoryService();
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.getUrl());
        if (siteEntity == null){
            siteRepositoryService.createSite(site, Status.INDEXED);
        }
        PageParser pageParser = new PageParser(url, site.getUrl(), serviceStore);
        pageParser.addOrUpdatePage();
    }

    @Override
    @Async
    public void startIndexing() {
        checkAndDeleteSiteIfPresent(sitesList);
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        parserList = new ArrayList<>();
        sitesList.getSites().forEach(site -> {
            SiteRunnable worker = new SiteRunnable(site, serviceStore);
            Future<?> future = executor.submit(worker);
            Parser parser = new Parser();
            parser.setDomain(site.getUrl());
            parser.setFuture(future);
            parser.setWorker(worker);
            parserList.add(parser);
        });
        executor.shutdown();
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
    public List<Parser> getParserList() {
        return this.parserList;
    }

    @Override
    public ExecutorService getExecutor() {
        return this.executor;
    }

    @Autowired
    public void setServiceStore(ServiceStore serviceStore){this.serviceStore = serviceStore;}
}
