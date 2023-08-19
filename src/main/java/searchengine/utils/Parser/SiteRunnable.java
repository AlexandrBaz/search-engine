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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@Component
@Scope("prototype")
public class SiteRunnable implements Runnable {

    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final Site site;
    private final CopyOnWriteArrayList<Page> pageEntityList;
    private final ArrayList<String> uniqUrlList;
    ForkJoinPool forkJoinPool;

    public SiteRunnable(Site site, @NotNull ServiceStore serviceStore) {
        this.pageEntityList = new CopyOnWriteArrayList<>();
        this.uniqUrlList = new ArrayList<>();
        this.site = site;
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
    }

    @Override
    public void run() {
        siteRepositoryService.createSite(site);
        long start = System.currentTimeMillis();
//        uniqueUrls.add(site.getUrl());
//        SiteParser siteParser = new SiteParser(site.getUrl(), site.getUrl(), uniqueUrls, pageEntityList, pageRepositoryService);
//        forkJoinPool = new ForkJoinPool();
//        forkJoinPool.invoke(siteParser);

        ListSiteParser listSiteParser = new ListSiteParser(Collections.singletonList(site.getUrl()), site.getUrl(), pageEntityList, uniqUrlList, this);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(listSiteParser);
        System.out.println("fjp end");
        if (forkJoinPool.isQuiescent()){
            synchronized (pageEntityList) {
                pageRepositoryService.addListPageEntity(pageEntityList, site.getUrl());
                System.out.println("completed " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
                System.out.println(uniqUrlList.size() + " from SiteRunnable");
            }
        }


//        System.out.println("completed adding to base" + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
//        new LemmaCollect(serviceStore).collectMapsLemmas(siteRepositoryService.getSiteEntityByDomain(site.getUrl()));
//        System.out.println("lemma finder completed for " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
//        new LemmaRank(serviceStore).lemmaRankBySite(site.getUrl());
//        System.out.println("rank completed for " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
//        siteRepositoryService.siteIndexComplete(site.getUrl());
//        }
    }


    public void stopForkJoin() {
        Thread.currentThread().interrupt();
        getForkJoinPool().shutdownNow();
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

    public PageRepositoryService getPageRepositoryService() {
        return this.pageRepositoryService;
    }


}