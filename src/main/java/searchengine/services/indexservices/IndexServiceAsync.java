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
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexservices.lemma.LemmaFinder;
import searchengine.services.indexservices.parser.PageParser;
import searchengine.services.indexservices.parser.Parser;
import searchengine.services.indexservices.parser.SiteRunnable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Log4j2
@Getter
public class IndexServiceAsync {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaFinder lemmaFinder;
    private final SitesList sitesList;
    private final PageParser pageParser;
    private final AppConfig appConfig;
    private ExecutorService executor;
    private List<Parser> parserList;

    @Autowired
    public IndexServiceAsync(SiteRepository siteRepository, PageRepository pageRepository,
                             LemmaRepository lemmaRepository, IndexRepository indexRepository,
                             LemmaFinder lemmaFinder, SitesList sitesList,
                             PageParser pageParser, AppConfig appConfig) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaFinder = lemmaFinder;
        this.sitesList = sitesList;
        this.pageParser = pageParser;
        this.appConfig = appConfig;
    }

    @Async
    public void parsePage(String url, @NotNull Site site) {
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl()).orElse(null);
        if (siteEntity == null){
            createSite(site, Status.INDEXED);
        }
        pageParser.setUrl(url);
        pageParser.setDomain(site.getUrl());
        pageParser.setPath(url.replaceAll(site.getUrl(), "/"));
        pageParser.addOrUpdatePage();
    }

    public synchronized void createSite(@NotNull Site site, Status status) {
        SiteEntity newSiteEntity = new SiteEntity();
        newSiteEntity.setName(site.getName());
        newSiteEntity.setUrl(site.getUrl());
        newSiteEntity.setStatus(status);
        newSiteEntity.setStatusTime(LocalDateTime.now());
        newSiteEntity.setLastError(null);
        siteRepository.saveAndFlush(newSiteEntity);
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

    public void checkAndDeleteSiteIfPresent(@NotNull SitesList sitesList) {
        sitesList.getSites().forEach(site -> {
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl()).orElse(null);
            if (siteEntity != null) {
                List<Long> pageEntityListId = pageRepository.findAllBySite(siteEntity).stream().map(PageEntity::getId).toList();
                log.info("Deleting IndexEntities for {}", siteEntity.getName());
                indexRepository.deleteAllByIdInBatch(pageEntityListId);
                log.info("Deleting LemmaEntities for {}", siteEntity.getName());
                lemmaRepository.deleteAllByIdInBatch(pageEntityListId);
                log.info("Deleting PageEntities for {}", siteEntity.getName());
                pageRepository.deleteAllByIdInBatch(pageEntityListId);
                log.info("Deleting SiteEntities for {}", siteEntity.getName());
                siteRepository.delete(siteEntity);
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
