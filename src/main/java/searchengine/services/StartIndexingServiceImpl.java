package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.index.TrueResponse;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;
import searchengine.model.Status;
import searchengine.services.sitehandler.SiteMapHandler;
import searchengine.services.sitehandler.SiteToCrawl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;


@Service
public class StartIndexingServiceImpl implements StartIndexingService {
    SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    SiteMapHandler siteMapHandler;
    List<Thread> threadList = new ArrayList<>();

    public StartIndexingServiceImpl(SitesList sitesList, SiteRepository siteRepository, PageRepository pageRepository, SiteMapHandler siteMapHandler) {
        this.sitesList = sitesList;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.siteMapHandler = siteMapHandler;
    }

    @Override
    public IndexResponse startIndexing() {
        sitesList.getSites().forEach(site -> {
            createSite(site);
                threadList.add(new Thread(() -> {
                    ForkJoinPool pool = new ForkJoinPool();
                    SiteToCrawl siteToCrawl = new SiteToCrawl();
                    siteToCrawl.setDomain(site.getUrl());
                    siteToCrawl.setUrlToCrawl(site.getUrl());
                    SiteMapHandler siteMapHandler = new SiteMapHandler(siteToCrawl, siteRepository, pageRepository, sitesList);
                    pool.invoke(siteMapHandler);
                }));
        });
        threadList.forEach(Thread::start);
        IndexResponse indexResponse = new IndexResponse();
        indexResponse.setResult(true);
        return indexResponse;
    }

    @Override
    public Boolean stopIndexing() {
        threadList.forEach(thread -> {
            if (thread.isAlive()){

                System.out.println("Stop Stop Stop");
                thread.interrupt();
                System.out.println(thread.isInterrupted());

            }
        });
        return true;
    }

    @Override
    public Boolean indexPage() {
        return null;
    }

    public void createSite(searchengine.config.Site siteToIndex) {
        boolean siteIsPresent = siteRepository.findByUrl(siteToIndex.getUrl()).isPresent();
        if (!siteIsPresent) {
            SiteEntity addSite = new SiteEntity();
            addSite.setName(siteToIndex.getName());
            addSite.setUrl(siteToIndex.getUrl());
            addSite.setStatus(Status.INDEXING);
            addSite.setStatusTime(LocalDateTime.now());
            addSite.setLastError(null);
            siteRepository.save(addSite);
        }
    }


}
//    compute - ForkJoinWorkerThread.currentThread().interrupt();