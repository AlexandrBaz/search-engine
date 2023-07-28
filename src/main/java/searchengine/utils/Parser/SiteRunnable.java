package searchengine.utils.Parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.services.SiteRepositoryService;
import searchengine.services.SiteRepositoryServiceImpl;
import searchengine.utils.ServiceStore;

import java.util.concurrent.ForkJoinPool;

@Component
@Scope("prototype")
public class SiteRunnable implements Runnable {

    private SiteRepositoryService siteRepositoryService;
    private final Site site;
    private ServiceStore serviceStore;
    ForkJoinPool forkJoinPool;

    public SiteRunnable(Site site, ServiceStore serviceStore) {
        this.site = site;
        this.serviceStore = serviceStore;
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
    }

    @Override
    public void run() {
        siteRepositoryService.createSite(site);
        long start = System.currentTimeMillis();
//        SiteMapHandler siteMapHandler = new SiteMapHandler(site.getUrl(), site.getUrl(), serviceStore);
        SiteParser siteParser = new SiteParser(site.getUrl(), site.getUrl(), serviceStore);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteParser);
        siteRepositoryService.siteIndexComplete(site.getUrl());
        System.out.println("completed " + site.getUrl() + " Time Elapsed -> " + (start-System.currentTimeMillis()) + " ms");
//        new LemmaCollect(allEntityDAO).collectLemmas(siteEntityDAO.getSiteEntity(site.getUrl()));
//        System.out.println("lemma finder completed for " + site.getUrl());
//        }
    }

    private ParsedPage getRootPage(Site site){
        ParsedPage rootPage = new ParsedPage();
        rootPage.setDomain(site.getUrl());
        rootPage.setUrlToParse(site.getUrl());
        rootPage = new JsoupParser().getDocument(rootPage);
        return rootPage;
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