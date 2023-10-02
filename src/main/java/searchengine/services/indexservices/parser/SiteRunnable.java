package searchengine.services.indexservices.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import searchengine.config.AppConfig;
import searchengine.config.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexservices.IndexServiceAsync;
import searchengine.services.indexservices.lemma.IndexCollect;
import searchengine.services.indexservices.lemma.LemmaCollect;
import searchengine.services.indexservices.lemma.LemmaFinder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Component
@Getter
@Setter
@Log4j2
public class SiteRunnable implements Runnable {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaFinder lemmaFinder;
    private final IndexServiceAsync indexServiceAsync;
    private AppConfig appConfig;
    private ForkJoinPool forkJoinPool;
    private SiteParser siteParser;
    private final Map<String, PageEntity> pageEntityMap;
    private final Set<String> pageEntityAlreadyParsed;
    private final Site site;
    private final Set<String> uniqUrl;
    private Boolean parseActive = true;

    public SiteRunnable(Site site, @NotNull IndexServiceAsync indexServiceAsync) {
        this.indexServiceAsync = indexServiceAsync;
        this.uniqUrl = new HashSet<>();
        this.pageEntityMap = new ConcurrentHashMap<>();
        this.pageEntityAlreadyParsed = new HashSet<>();
        this.site = site;
        this.siteRepository = indexServiceAsync.getSiteRepository();
        this.pageRepository = indexServiceAsync.getPageRepository();
        this.lemmaRepository = indexServiceAsync.getLemmaRepository();
        this.indexRepository = indexServiceAsync.getIndexRepository();
        this.appConfig = indexServiceAsync.getAppConfig();
        this.lemmaFinder = indexServiceAsync.getLemmaFinder();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        indexServiceAsync.createSite(site, Status.INDEXING);
        siteParser = new SiteParser(Collections.singletonList(site.getUrl()), this);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteParser);
        if (forkJoinPool.isQuiescent()) {
            saveLastPageEntity(pageEntityMap);
            pageEntityMap.clear();
            forkJoinPool.shutdownNow();
            log.info("Pars completed {}. Time Elapsed: {} ms", site.getUrl(), (System.currentTimeMillis() - start));
        }
        new LemmaCollect(this).collectMapsLemmas(getSiteEntity());
        log.info("LemmaFinder completed {}. Time Elapsed: {} ms", site.getUrl(), (System.currentTimeMillis() - start));
        new IndexCollect(this).lemmaRankBySite();
        log.info("IndexCollect completed {}. Time Elapsed: {} ms", site.getUrl(), (System.currentTimeMillis() - start));
        siteIndexComplete(site);
    }

    public void stopForkJoin() {
        getForkJoinPool().shutdownNow();
        parseActive = false;
    }

    private synchronized void saveLastPageEntity(@NotNull Map<String, PageEntity> pageEntityMap) {
        List<PageEntity> pageEntityList = pageEntityMap.values().stream().toList();
        pageRepository.saveAllAndFlush(pageEntityList);
    }

    private void siteIndexComplete(@NotNull Site site) {
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl()).orElse(null);
        if (siteEntity != null) {
            siteEntity.setStatus(Status.INDEXED);
            siteEntity.setLastError(null);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(siteEntity);
        }
    }

    public SiteEntity getSiteEntity() {
        return siteRepository.findByUrl(site.getUrl()).orElse(null);
    }

    public String getDomain() {
        return site.getUrl();
    }
}