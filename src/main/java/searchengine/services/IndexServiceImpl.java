package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.services.sitehandler.Handler;
import searchengine.services.sitehandler.PageHandler;
import searchengine.services.sitehandler.SiteMapHandler;
import searchengine.services.sitehandler.SiteRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class IndexServiceImpl implements IndexService {

    SiteEntityDAO siteEntityDAO;
    PageEntityDAO pageEntityDAO;
    SitesList sitesList;
    SiteMapHandler siteMapHandler;
    ExecutorService executor;
    Handler handler;
    List<Handler> handlerList = new ArrayList<>();

    @Autowired
    public IndexServiceImpl(SitesList sitesList, SiteMapHandler siteMapHandler, SiteEntityDAO siteEntityDAO, PageEntityDAO pageEntityDAO) {
        this.sitesList = sitesList;
        this.siteMapHandler = siteMapHandler;
        this.siteEntityDAO = siteEntityDAO;
        this.pageEntityDAO = pageEntityDAO;
    }

    @Override
    public Boolean startIndexing() { // TODO реализовать повторный поиск
        if (!isStarted()) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            sitesList.getSites().forEach(site -> {
                SiteRunnable worker = new SiteRunnable(site, siteEntityDAO, pageEntityDAO, sitesList);
                Future<?> future = executor.submit(worker);
                handler = new Handler();
                handler.setDomain(site.getUrl());
                handler.setFuture(future);
                handler.setWorker(worker);
                handlerList.add(handler);
            });
            executor.shutdown();
            return true;
        } else {
            return false;
        }
    }


    @Override
    public Boolean stopIndexing() {
        if (isStarted()) {
            handlerList.forEach(handler -> {
                handler.getWorker().stopForkJoin();
                if (!handler.getFuture().isDone()) {
                    siteEntityDAO.stopIndexEntity(handler.getDomain());
                }
            });
            executor.shutdownNow();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean indexPage(String url) {
        PageHandler pageHandler = new PageHandler(siteEntityDAO, pageEntityDAO);
        String domain = sitesList.getSites().stream()
                .map(Site::getUrl)
                .filter(url::contains)
                .findFirst()
                .orElse(null);
        if (domain != null) {
            pageHandler.addUpdatePage(url, domain, sitesList);
            return true;
        } else {
            return false;
        }
    }


    public boolean isStarted() {
        for (Handler hand : handlerList){
            if (!hand.getFuture().isDone()){
                return true;
            }
        }
        return false;
    }
}
