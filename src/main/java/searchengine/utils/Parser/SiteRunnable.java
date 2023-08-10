package searchengine.utils.Parser;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.dto.parser.Page;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.utils.ServiceStore;
import searchengine.utils.lemma.LemmaCollect;
import searchengine.utils.lemma.LemmaRank;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Component
@Scope("prototype")
public class SiteRunnable implements Runnable {

    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;

    private final Site site;
    private final ServiceStore serviceStore;
    private final TreeSet<String> uniqueUrls;
    private final TreeMap<String, Page> pageList;
    ForkJoinPool forkJoinPool;

    public SiteRunnable(Site site, @NotNull ServiceStore serviceStore) {
        this.uniqueUrls = new TreeSet<>();
        this.pageList = new TreeMap<>();
        this.site = site;
        this.serviceStore = serviceStore;
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
    }

    @Override
    public void run() {
        siteRepositoryService.createSite(site);
        long start = System.currentTimeMillis();
        uniqueUrls.add(site.getUrl());
        SiteParser siteParser = new SiteParser(site.getUrl(), site.getUrl(), uniqueUrls, pageList);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteParser);

        System.out.println("completed " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
        System.out.println(uniqueUrls.size() + " from SiteRunnable");
        pageRepositoryService.addListPageEntity(pageList, site.getUrl());
        System.out.println("completed adding to base" + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
        new LemmaCollect(serviceStore).collectMapsLemmas(siteRepositoryService.getSiteEntityByDomain(site.getUrl()));
        System.out.println("lemma finder completed for " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
        new LemmaRank(serviceStore).lemmaRankBySite(site.getUrl());
        System.out.println("rank completed for " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
        siteRepositoryService.siteIndexComplete(site.getUrl());
//        }
    }


    public void stopForkJoin() {
        Thread.currentThread().interrupt();
        getForkJoinPool().shutdownNow();
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }
}