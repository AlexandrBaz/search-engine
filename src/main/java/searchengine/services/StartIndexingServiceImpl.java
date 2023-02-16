package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.index.IndexResponse;
import searchengine.model.IndexRepository;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.sitehandler.Node;
import searchengine.services.sitehandler.SiteMapHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class StartIndexingServiceImpl implements StartIndexingService {
    SitesList sitesList;
    private final IndexRepository indexRepository;


    public StartIndexingServiceImpl(SitesList sitesList, IndexRepository indexRepository) {
        this.sitesList = sitesList;
        this.indexRepository = indexRepository;
    }

    @Override
    public IndexResponse getStart() {
        List<Thread> threadList = new ArrayList<>();
        sitesList.getSites().forEach(site -> {
            if (createSite(site)) {
                threadList.add(new Thread(() -> {
                    Node root = new Node(site.getUrl(), site.getUrl());
                    ForkJoinPool pool = new ForkJoinPool();
                    SiteMapHandler siteMapHandler = new SiteMapHandler(root, indexRepository);
                    pool.invoke(siteMapHandler);
                }));
            }
        });
        threadList.forEach(Thread::start);
        return null;
    }

    public boolean createSite(searchengine.config.Site siteToIndex) {
        boolean siteIsPresent = indexRepository.findByUrl(siteToIndex.getUrl()).isPresent();
        if (!siteIsPresent) {
            Site addSite = new Site();
            addSite.setName(siteToIndex.getName());
            addSite.setUrl(siteToIndex.getUrl());
            addSite.setStatus(Status.INDEXING);
            addSite.setStatusTime(LocalDateTime.now());
            addSite.setLastError(null);
            indexRepository.save(addSite);
            return addToThread();
        }
        return false;
    }

    public boolean addToThread() {
        return false;
    }
}
//    Я просто в compute  вызываю ForkJoinWorkerThread.currentThread().interrupt(); Стопорится мгновенно