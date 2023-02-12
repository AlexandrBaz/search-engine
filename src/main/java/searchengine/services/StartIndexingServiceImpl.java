package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.index.IndexResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class StartIndexingServiceImpl implements StartIndexingService {
    SitesList sitesList;

    public StartIndexingServiceImpl(SitesList sitesList) {
        this.sitesList = sitesList;
    }

    @Override
    public IndexResponse getStart() {
        List<Thread> threadList = new ArrayList<>();
        sitesList.getSites().forEach(site -> threadList.add(new Thread(() -> {
            ForkJoinPool pool = new ForkJoinPool();
            SiteMapHandler siteMapHandler = new SiteMapHandler(site.getUrl());
            pool.invoke(siteMapHandler);

        })));
        threadList.forEach(Thread::start);
        return null;
    }
}
