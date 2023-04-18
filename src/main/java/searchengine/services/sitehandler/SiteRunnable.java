package searchengine.services.sitehandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.services.PageEntityDAO;
import searchengine.services.SiteEntityDAO;

import java.util.concurrent.ForkJoinPool;

@Component

public class SiteRunnable implements Runnable {

    private final SiteEntityDAO siteEntityDAO;
    private final PageEntityDAO pageEntityDAO;
    private final SitesList sitesList;
    Site site;
    ForkJoinPool forkJoinPool;
    SiteMapHandler siteMapHandler;

    @Autowired
    public SiteRunnable(Site site, SiteEntityDAO siteEntityDAO, PageEntityDAO pageEntityDAO, SitesList sitesList) {
        this.site = site;
        this.siteEntityDAO = siteEntityDAO;
        this.pageEntityDAO = pageEntityDAO;
        this.sitesList = sitesList;
    }

    @Override
    public void run() {
        siteEntityDAO.createSite(site);
        forkJoinPool = new ForkJoinPool();
        SiteToCrawl siteToCrawl = new SiteToCrawl();
        siteToCrawl.setDomain(site.getUrl());
        siteToCrawl.setUrlToCrawl(site.getUrl());
        siteMapHandler = new SiteMapHandler(siteToCrawl, sitesList, siteEntityDAO, pageEntityDAO);
        forkJoinPool.invoke(siteMapHandler);
        if (siteMapHandler.isDone()) {
            siteEntityDAO.indexComplete(site.getUrl());
        }
    }

    public void stopForkJoin() {
        Thread.currentThread().interrupt();
        getForkJoinPool().shutdownNow();
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }
}