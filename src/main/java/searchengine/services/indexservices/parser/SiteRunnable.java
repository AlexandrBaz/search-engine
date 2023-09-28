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
import searchengine.services.indexservices.IndexServiceAsync;
import searchengine.services.indexservices.lemma.IndexCollect;
import searchengine.services.indexservices.lemma.LemmaCollect;
import searchengine.services.indexservices.lemma.LemmaFinder;
import searchengine.services.reposervices.IndexRepositoryService;
import searchengine.services.reposervices.LemmaRepositoryService;
import searchengine.services.reposervices.PageRepositoryService;
import searchengine.services.reposervices.SiteRepositoryService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Component
@Getter
@Setter
@Log4j2
public class SiteRunnable implements Runnable {

    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final LemmaFinder lemmaFinder;
    private AppConfig appConfig;
    private ForkJoinPool forkJoinPool;
    private SiteParser siteParser;
    private final Map<String, PageEntity> pageEntityMap;
    private final Set<String> pageEntityAlreadyParsed;
    private final Site site;
    private final Set<String> uniqUrl;
    private Boolean parseActive = true;

    public SiteRunnable(Site site, @NotNull IndexServiceAsync indexServiceAsync) {
        this.uniqUrl = new HashSet<>();
        this.pageEntityMap = new ConcurrentHashMap<>();
        this.pageEntityAlreadyParsed = new HashSet<>();
        this.site = site;
        this.siteRepositoryService = indexServiceAsync.getSiteRepositoryService();
        this.pageRepositoryService = indexServiceAsync.getPageRepositoryService();
        this.lemmaRepositoryService = indexServiceAsync.getLemmaRepositoryService();
        this.indexRepositoryService = indexServiceAsync.getIndexRepositoryService();
        this.appConfig = indexServiceAsync.getAppConfig();
        this.lemmaFinder = indexServiceAsync.getLemmaFinder();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        siteRepositoryService.createSite(site, Status.INDEXING);
        siteParser = new SiteParser(Collections.singletonList(site.getUrl()), this);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteParser);
        if (forkJoinPool.isQuiescent()) {
            pageRepositoryService.savePageEntityMap(pageEntityMap);
            pageEntityMap.clear();
            forkJoinPool.shutdown();
            log.info("Pars completed {}. Time Elapsed: {} ms", site.getUrl(), (System.currentTimeMillis() - start));
        }
        new LemmaCollect(this).collectMapsLemmas(getSiteEntity());
        log.info("LemmaFinder completed {}. Time Elapsed: {} ms", site.getUrl(), (System.currentTimeMillis() - start));
        new IndexCollect(this).lemmaRankBySite();
        log.info("IndexCollect completed {}. Time Elapsed: {} ms", site.getUrl(), (System.currentTimeMillis() - start));
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