package searchengine.utils.parser;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.*;
import searchengine.utils.ServiceStore;
import searchengine.utils.lemma.LemmaCollect;
import searchengine.utils.lemma.LemmaRank;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Component
@Getter
@Log4j2
public class SiteRunnable implements Runnable {

    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final IndexServiceAsync indexServiceAsync;
    private ForkJoinPool forkJoinPool;
    private SiteParserBatch siteParserBatch;
    private final ConcurrentHashMap<String, PageEntity> pageEntityMap;
    private final Set<String> pageEntityAlreadyParsed;
    private final Site site;
    private final Set<String> uniqUrl;
    private Boolean parseActive = true;

    public SiteRunnable(Site site, @NotNull ServiceStore serviceStore, IndexServiceAsync indexServiceAsync) {
        this.uniqUrl = new HashSet<>();
        this.pageEntityMap = new ConcurrentHashMap<>();
        this.pageEntityAlreadyParsed = new HashSet<>();
        this.site = site;
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
        this.indexRepositoryService = serviceStore.getIndexRepositoryService();
        this.indexServiceAsync = indexServiceAsync;
    }

    @Override
    public void run() {
        siteRepositoryService.createSite(site, Status.INDEXING);
        long start = System.currentTimeMillis();
//        siteParser = new SiteParser(Collections.singletonList(site.getUrl()), this);
        siteParserBatch = new SiteParserBatch(Collections.singletonList(site.getUrl()), this);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteParserBatch);
        if (forkJoinPool.isQuiescent()) {
            pageRepositoryService.savePageEntityMap(pageEntityMap);
            pageEntityMap.clear();
            forkJoinPool.shutdown();
            log.info("completed " + site.getUrl() + " Time Elapsed -> " + (System.currentTimeMillis() - start) + " ms");
            log.info(uniqUrl.size() + " from SiteRunnable");
        }
        new LemmaCollect(this).collectMapsLemmas(getSiteEntity());
        log.info("lemma finder completed for " + site.getUrl() + " Time Elapsed -> " + (System.currentTimeMillis() - start) + " ms");
        new LemmaRank(this).lemmaRankBySite();
        log.info("rank completed for " + site.getUrl() + " Time Elapsed -> " + (System.currentTimeMillis() - start) + " ms");
        siteRepositoryService.siteIndexComplete(site.getUrl());
    }

    public void stopForkJoin() {
        getForkJoinPool().shutdownNow();
        parseActive = false;
    }

    public SiteEntity getSiteEntity() {
        return siteRepositoryService.getSiteEntityByDomain(site.getUrl());
    }

    public String getDomain() {
        return site.getUrl();
    }
}