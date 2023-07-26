package searchengine.services.sitehandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.dao.AllEntityDAO;
import searchengine.dao.SiteEntityDAO;
import searchengine.services.lemma.LemmaCollect;

import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

@Component
public class SiteRunnable implements Runnable {

    private final SiteEntityDAO siteEntityDAO;
    private final AllEntityDAO allEntityDAO;
    Site site;
    ForkJoinPool forkJoinPool;

    @Autowired
    public SiteRunnable(Site site, AllEntityDAO allEntityDAO) {
        this.site = site;
        this.allEntityDAO = allEntityDAO;
        this.siteEntityDAO = allEntityDAO.getSiteEntityDAO();
    }

    @Override
    public void run() {
        siteEntityDAO.createSite(site);
        long start = System.currentTimeMillis();
        SiteMapHandler siteMapHandler = new SiteMapHandler(getRootPage(site), allEntityDAO);
        forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(siteMapHandler);
        siteEntityDAO.indexComplete(site.getUrl());
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

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }
}