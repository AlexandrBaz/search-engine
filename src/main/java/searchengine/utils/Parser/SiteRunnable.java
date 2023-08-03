package searchengine.utils.Parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.dto.parser.Page;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.services.SiteRepositoryServiceImpl;
import searchengine.utils.ServiceStore;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Component
@Scope("prototype")
public class SiteRunnable implements Runnable {

    private SiteRepositoryService siteRepositoryService;
    private PageRepositoryService pageRepositoryService;

    private final Site site;
    private final ServiceStore serviceStore;
    private final TreeSet<String> uniqueUrls = new TreeSet<>();
    private static final TreeMap<String, Page> pageList = new TreeMap<>();
    ForkJoinPool forkJoinPool;

    public SiteRunnable(Site site, ServiceStore serviceStore) {
        this.site = site;
        this.serviceStore = serviceStore;
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
    }

    @Override
    public void run() {
        siteRepositoryService.createSite(site);
        long start = System.currentTimeMillis();
//        SiteMapHandler siteMapHandler = new SiteMapHandler(site.getUrl(), site.getUrl(), serviceStore);
        uniqueUrls.add(site.getUrl());
        SiteParser siteParser = new SiteParser(site.getUrl(), site.getUrl(), uniqueUrls, pageList);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteParser);
        siteRepositoryService.siteIndexComplete(site.getUrl());
        System.out.println("completed " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
        System.out.println(uniqueUrls.size() + " from SiteRunnable");
        pageRepositoryService.addListPageEntity(pageList, site.getUrl());
        System.out.println("completed adding to base" + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
//        new LemmaCollect(allEntityDAO).collectLemmas(siteEntityDAO.getSiteEntity(site.getUrl()));
//        System.out.println("lemma finder completed for " + site.getUrl());
//        }
    }


    public void stopForkJoin() {
        Thread.currentThread().interrupt();
        getForkJoinPool().shutdownNow();
    }

    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService){
        this.siteRepositoryService = siteRepositoryService;
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }
}