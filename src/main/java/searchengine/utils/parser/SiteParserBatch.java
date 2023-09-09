package searchengine.utils.parser;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class SiteParserBatch extends RecursiveAction {

    private final List<String> listUrlsToParse;
    private final String domain;
    private final Set<String> uniqUrl;
    private final ConcurrentHashMap<String, PageEntity> pageEntityMap;
    private final Set<String> pageEntityAlreadyParsed;
    private final SiteEntity siteEntity;
    private final SiteRunnable siteRunnable;
    ReentrantLock lock = new ReentrantLock();


    public SiteParserBatch(List<String> listUrlsToParse, ConcurrentHashMap<String, PageEntity> pageEntityMap, @NotNull SiteRunnable siteRunnable) {
        this.listUrlsToParse = listUrlsToParse;
        this.siteRunnable = siteRunnable;
        this.domain = siteRunnable.getDomain();
        this.uniqUrl = siteRunnable.getUniqUrl();
        this.pageEntityMap = pageEntityMap;
        this.pageEntityAlreadyParsed = siteRunnable.getPageEntityAlreadyParsed();
        this.siteEntity = siteRunnable.getSiteEntity();
    }

    @Override
    protected void compute() {
        if (parseActive()) {
            CopyOnWriteArrayList<SiteParserBatch> listSiteParsersBatch = getTasksAndFork();
            for (SiteParserBatch task : listSiteParsersBatch) {
                task.join();
            }
        } else {
            log.info(Thread.currentThread().getName() + " " + Thread.currentThread().isInterrupted() + " stop");
        }
    }

    public CopyOnWriteArrayList<SiteParserBatch> getTasksAndFork() {
        CopyOnWriteArrayList<SiteParserBatch> tasks = new CopyOnWriteArrayList<>();
        listUrlsToParse.forEach(url -> {
            String modifiedUrl = url.toLowerCase();
            lock.lock();
            try {
                if (urlIsValidAndUnique(modifiedUrl)) {
                    List<String> nextListUrToParse = parseUrl(modifiedUrl);
                    SiteParserBatch siteParserBatch = new SiteParserBatch(nextListUrToParse, pageEntityMap, siteRunnable);
                    siteParserBatch.fork();
                    tasks.add(siteParserBatch);
                    log.info(pageEntityAlreadyParsed.size() + " pageEntityAlreadyParsed " + modifiedUrl);
                }
            } finally {
                lock.unlock();
            }
        });
        return tasks;
    }

    private boolean urlIsValidAndUnique(String url) {
        return urlIsValid(url) && urlIsUnique(url) && parseActive();
    }

    private boolean urlIsValid(@NotNull String url) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.startsWith(domain)
                && (url.endsWith("/")
                || url.endsWith("html"));
    }

    private boolean urlIsUnique(String url) {
        return !pageEntityAlreadyParsed.contains(url);
    }

    private boolean parseActive() {
        if (!siteRunnable.getParseActive()) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private @NotNull List<String> parseUrl(@NotNull String url) {
        List<String> urlList = new CopyOnWriteArrayList<>();
        if (!pageEntityAlreadyParsed.contains(url) && !uniqUrl.contains(url)) {
            pageEntityAlreadyParsed.add(url);
            threadSleep();
            try {
                Connection.Response response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .timeout(100000)
                        .ignoreHttpErrors(true)
                        .execute();
                Document document = response.parse();
                createPage(document, url, response.statusCode());
                Elements elements = document.select("a[href]");
                urlList = elements.stream().map(absUrl -> absUrl.attr("abs:href")).toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return urlList;
    }

    private void threadSleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void createPage(@NotNull Document document, @NotNull String url, int statusCode) {
        if (!uniqUrl.contains(url)) {
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSite(siteEntity);
            pageEntity.setPath(url.replaceAll(domain, "/"));
            pageEntity.setCode(statusCode);
            pageEntity.setContent(document.outerHtml());
            pageEntityMap.put(url, pageEntity);
            uniqUrl.add(url);
        }
        if (pageEntityMap.size() == 100) {
            log.info("total uniqUrl: " + uniqUrl.size() + " " + pageEntityMap.size());
            List<PageEntity> pageEntityList = new ArrayList<>(pageEntityMap.values().stream().toList());
            pageEntityMap.clear();
            siteRunnable.getPageRepositoryService().addListPageEntity(pageEntityList);
            siteRunnable.getSiteRepositoryService().updateSite(siteEntity);
            pageEntityList.clear();
        }
    }
}
