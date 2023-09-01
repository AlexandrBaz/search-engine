package searchengine.utils.parser;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.PageRepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log4j2
public class SiteParser extends RecursiveAction {
    private final List<String> listUrlsToParse;
    private final String domain;
    private final PageRepositoryService pageRepositoryService;
    private final Set<String> uniqUrl;
    private CopyOnWriteArrayList<SiteParser> listSiteParsers;
    private final SiteEntity siteEntity;
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final SiteRunnable siteRunnable;

    public SiteParser(List<String> listUrlsToParse, SiteRunnable siteRunnable) {
        this.listUrlsToParse = listUrlsToParse;
        this.siteRunnable = siteRunnable;
        this.domain = siteRunnable.getDomain();
        this.uniqUrl = siteRunnable.getUniqUrl();
        this.listSiteParsers = new CopyOnWriteArrayList<>();
        this.siteEntity = siteRunnable.getSiteEntity();
        this.pageRepositoryService = siteRunnable.getPageRepositoryService();
    }

    @Override
    protected void compute() {
        if (parseActive()) {
            listSiteParsers = getTasksAndFork();
            for (SiteParser task : listSiteParsers) {
                task.join();
            }
        } else {
            log.info(Thread.currentThread().getName() + " " + Thread.currentThread().isInterrupted() + " stop");
        }
    }

    public CopyOnWriteArrayList<SiteParser> getTasksAndFork() {
        CopyOnWriteArrayList<SiteParser> tasks = new CopyOnWriteArrayList<>();
        listUrlsToParse.forEach(url -> {
            if (urlIsValidAndUnique(url)) {
                tasks.add(getChild(url));
            }
        });
        return tasks;
    }

    private boolean urlIsValidAndUnique(String url) {
        readWriteLock.readLock().lock();
        boolean isValidAndUnique = false;
        try {
            if (urlIsValid(url) && urlIsUnique(url) && parseActive()) {
                isValidAndUnique = true;
                uniqUrl.add(url);
            }

        } finally {
            readWriteLock.readLock().unlock();
        }
        return isValidAndUnique;
    }

    private boolean urlIsUnique(String url) {
        return !uniqUrl.contains(url);
    }

    private boolean parseActive() {
        if (!siteRunnable.getParseActive()) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private boolean urlIsValid(@NotNull String url) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.startsWith(domain)
                && (url.endsWith("/")
                || url.endsWith("html"));
    }


    private @NotNull SiteParser getChild(String url) {
        readWriteLock.writeLock().lock();
        SiteParser listSiteParser;
        try {
            CopyOnWriteArrayList<String> listChildUrlsToParse = parseUrl(url);
            listSiteParser = new SiteParser(listChildUrlsToParse, siteRunnable);
            listSiteParser.fork();
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return listSiteParser;
    }

    private @NotNull CopyOnWriteArrayList<String> parseUrl(String url) {
        CopyOnWriteArrayList<String> urlList;
        threadSleep();
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(10000)
                    .ignoreHttpErrors(true)
                    .execute();
            Document document = response.parse();
            createPage(document, url, response.statusCode());
            Elements elements = document.select("a[href]");
            urlList = new CopyOnWriteArrayList<>(elements.stream().map(absUrl -> absUrl.attr("abs:href")).toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return urlList;
    }

    private void threadSleep() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void createPage(@NotNull Document document, @NotNull String url, int statusCode) {
        readWriteLock.writeLock().lock();
        try {
            String path = url.replaceAll(domain, "/");
            if (!pageRepositoryService.pageEntityIsPresent(path, domain)) {
                PageEntity pageEntity = new PageEntity();
                pageEntity.setSite(siteEntity);
                pageEntity.setPath(url.replaceAll(domain, "/"));
                pageEntity.setCode(statusCode);
                pageEntity.setContent(document.outerHtml());
                pageRepositoryService.savePageEntity(pageEntity);
                log.info("total uniqUrl: " + uniqUrl.size() + " for " + domain + " write: " + url);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}