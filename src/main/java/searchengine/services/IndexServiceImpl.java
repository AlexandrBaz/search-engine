package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.utils.Parser.Handler;
import searchengine.utils.Parser.PageHandler;
import searchengine.utils.Parser.SiteRunnable;
import searchengine.utils.ServiceStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class IndexServiceImpl implements IndexService {
    private SitesList sitesList;
    private ExecutorService executor;
    private SiteRepositoryService siteRepositoryService;
    private ServiceStore serviceStore;
    Handler handler;
    List<Handler> handlerList = new ArrayList<>();

//    @Autowired
//    public IndexServiceImpl(SitesList sitesList) {
//        this.sitesList = sitesList;
//    }

    @Override
    public Boolean startIndexing() { // TODO реализовать повторный поиск
        if (!isStarted()) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            sitesList.getSites().forEach(site -> {
                SiteRunnable worker = new SiteRunnable(site, serviceStore);
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
                    siteRepositoryService.stopIndexingThisEntity(handler.getDomain());
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
        PageHandler pageHandler = new PageHandler();
        String domain = sitesList.getSites().stream()
                .map(Site::getUrl)
                .filter(url::contains)
                .findFirst()
                .orElse(null);
        if (domain != null) {
            pageHandler.addUpdatePage(url, domain);
            return true;
        } else {
            return false;
        }
    }


    public boolean isStarted() {
        for (Handler hand : handlerList) {
            if (!hand.getFuture().isDone()) {
                return true;
            }
        }
        return false;
    }

    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService){
        this.siteRepositoryService = siteRepositoryService;
    }

    @Autowired
    public void setSitesList(SitesList sitesList){
        this.sitesList = sitesList;
    }

    @Autowired
    public void setServiceStore(ServiceStore serviceStore){
        this.serviceStore = serviceStore;
    }
}
