package searchengine.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.index.IndexResponse;
import searchengine.model.PageRepository;
import searchengine.model.SiteRepository;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.sitehandler.SiteMapHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@Scope("prototype")
public class StartIndexingServiceImpl implements StartIndexingService {
    SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    public StartIndexingServiceImpl(SitesList sitesList, SiteRepository siteRepository, PageRepository pageRepository) {
        this.sitesList = sitesList;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    public IndexResponse getStart() {
        List<Thread> threadList = new ArrayList<>();
        sitesList.getSites().forEach(site -> {
            createSite(site);
                threadList.add(new Thread(() -> {
                    ForkJoinPool pool = new ForkJoinPool();
                    SiteMapHandler siteMapHandler = new SiteMapHandler(site.getUrl(), siteRepository, pageRepository);
                    pool.invoke(siteMapHandler);
                }));
        });
        threadList.forEach(Thread::start);
        return null;
    }

    public void createSite(searchengine.config.Site siteToIndex) {
        boolean siteIsPresent = siteRepository.findByUrl(siteToIndex.getUrl()).isPresent();
        if (!siteIsPresent) {
            Site addSite = new Site();
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