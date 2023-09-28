package searchengine.services.indexservices;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.AppConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.indexservices.lemma.LemmaFinder;
import searchengine.services.indexservices.parser.PageParser;
import searchengine.services.indexservices.parser.Parser;
import searchengine.services.indexservices.parser.SiteRunnable;
import searchengine.services.reposervices.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Log4j2
@Getter
public class IndexServiceAsync {
    private final PageRepositoryService pageRepositoryService;
    private final SiteRepositoryService siteRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final LemmaFinder lemmaFinder;
    private final SitesList sitesList;
    private final PageParser pageParser;

    private final AppConfig appConfig;

    private ExecutorService executor;
    private List<Parser> parserList;

    @Autowired
    public IndexServiceAsync(PageRepositoryService pageRepositoryService, SiteRepositoryService siteRepositoryService,
                             IndexRepositoryService indexRepositoryService, LemmaRepositoryService lemmaRepositoryService,
                             LemmaFinder lemmaFinder, SitesList sitesList, PageParser pageParser, AppConfig appConfig) {
        this.pageRepositoryService = pageRepositoryService;
        this.siteRepositoryService = siteRepositoryService;
        this.indexRepositoryService = indexRepositoryService;
        this.lemmaRepositoryService = lemmaRepositoryService;
        this.lemmaFinder = lemmaFinder;
        this.sitesList = sitesList;
        this.pageParser = pageParser;
        this.appConfig = appConfig;
    }

    @Async
    public void parsePage(String url, @NotNull Site site) {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.getUrl());
        if (siteEntity == null){
            siteRepositoryService.createSite(site, Status.INDEXED);
        }
        pageParser.setUrl(url);
        pageParser.setDomain(site.getUrl());
        pageParser.setPath(url.replaceAll(site.getUrl(), "/"));
        pageParser.addOrUpdatePage();
    }

    @Async
    public void startIndexing() {
        checkAndDeleteSiteIfPresent(sitesList);
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        parserList = new ArrayList<>();
        sitesList.getSites().forEach(site -> {
            SiteRunnable worker = new SiteRunnable(site,this);
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
        sitesList.getSites().forEach(site -> {
            SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.getUrl());
            if (siteEntity != null) {
                List<Long> pageEntityListId = pageRepositoryService.getIdListPageEntity(siteEntity);
                log.info("Deleting IndexEntities for {}", siteEntity.getName());
                indexRepositoryService.deleteByIdListPageEntity(pageEntityListId);
                log.info("Deleting LemmaEntities for {}", siteEntity.getName());
                lemmaRepositoryService.deleteByIdListPageEntity(pageEntityListId);
                log.info("Deleting PageEntities for {}", siteEntity.getName());
                pageRepositoryService.deleteByIdListPageEntity(pageEntityListId);
                log.info("Deleting SiteEntities for {}", siteEntity.getName());
                siteRepositoryService.deleteSiteEntity(siteEntity);
            }
        });
    }

    public List<Parser> getParserList() {
        return this.parserList;
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }
}
