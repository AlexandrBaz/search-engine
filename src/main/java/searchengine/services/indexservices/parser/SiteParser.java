package searchengine.services.indexservices.parser;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class SiteParser extends RecursiveAction {

    private final List<String> listUrlsToParse;
    private final Set<String> uniqUrl;
    private final Set<String> pageEntityAlreadyParsed;
    private final SiteRunnable siteRunnable;
    private final ReentrantLock lock = new ReentrantLock();
    private final int parseBatchSize;
    private final String userAgent;
    private final int timeOut;
    private final boolean isIgnoreHttpErrors;
    private final int threadSleep;
    private final String mediaRegex;


    public SiteParser(List<String> listUrlsToParse, @NotNull SiteRunnable siteRunnable) {
        this.listUrlsToParse = listUrlsToParse;
        this.siteRunnable = siteRunnable;
        this.uniqUrl = siteRunnable.getUniqUrl();
        this.pageEntityAlreadyParsed = siteRunnable.getPageEntityAlreadyParsed();
        this.parseBatchSize = siteRunnable.getAppConfig().getParseBatchSize();
        this.userAgent = siteRunnable.getAppConfig().getUserAgent();
        this.timeOut = siteRunnable.getAppConfig().getTimeOut();
        this.isIgnoreHttpErrors = siteRunnable.getAppConfig().isIgnoreHttpErrors();
        this.threadSleep = siteRunnable.getAppConfig().getThreadSleep();
        this.mediaRegex = siteRunnable.getAppConfig().getMediaRegex();
    }

    @Override
    protected void compute() {
        if (parseActive()) {
            List<SiteParser> listSiteParsersBatch = getTasksAndFork();
            for (SiteParser task : listSiteParsersBatch) {
                task.join();
            }
        } else {
            log.info("Thread is STOP {}, {} ",Thread.currentThread().getName(), Thread.currentThread().isInterrupted());
        }
    }

    public List<SiteParser> getTasksAndFork() {
        List<SiteParser> tasks = new CopyOnWriteArrayList<>();
        listUrlsToParse.forEach(url -> {
            String modifiedUrl = url.toLowerCase();
            lock.lock();
            try {
                if (urlIsValidAndUnique(modifiedUrl)) {
                    List<String> nextListUrToParse = parseUrl(modifiedUrl);
                    SiteParser siteParserBatch = new SiteParser(nextListUrToParse, siteRunnable);
                    siteParserBatch.fork();
                    tasks.add(siteParserBatch);
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
        return !url.matches(mediaRegex)
                && url.startsWith(siteRunnable.getDomain())
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
                        .userAgent(userAgent)
                        .timeout(timeOut)
                        .ignoreHttpErrors(isIgnoreHttpErrors)
                        .execute();
                Document document = response.parse();
                createPage(document, url, response.statusCode());
                Elements elements = document.select("a[href]");
                urlList = elements.stream().map(absUrl -> absUrl.attr("abs:href")).toList();
            } catch (IOException e) {
                SiteEntity siteEntity = siteRunnable.getSiteEntity();
                siteEntity.setLastError(e.getMessage());
                siteRunnable.getSiteRepository().save(siteEntity);
                throw new RuntimeException(e);
            }
        }
        return urlList;
    }

    private void threadSleep() {
        try {
            Thread.sleep(threadSleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void createPage(@NotNull Document document, @NotNull String url, int statusCode) {
        Map<String, PageEntity> pageEntityMap = siteRunnable.getPageEntityMap();
        SiteEntity siteEntity = siteRunnable.getSiteEntity();
        if (!uniqUrl.contains(url)) {
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSite(siteEntity);
            pageEntity.setPath(url.replaceAll(siteRunnable.getDomain(), "/"));
            pageEntity.setCode(statusCode);
            pageEntity.setContent(document.outerHtml());
            pageEntityMap.put(url, pageEntity);
            uniqUrl.add(url);
        }
        checkAndWrite(pageEntityMap, siteEntity);
    }

    private synchronized void checkAndWrite(@NotNull Map<String, PageEntity> pageEntityMap, SiteEntity siteEntity) {
        if (pageEntityMap.size() == parseBatchSize) {
            log.info("Write to DB: total uniqUrl -> {} for domain -> {}",uniqUrl.size(), siteEntity.getName());
            List<PageEntity> pageEntityList = new ArrayList<>(pageEntityMap.values().stream().toList());
            pageEntityMap.clear();
            siteRunnable.getPageRepository().saveAllAndFlush(pageEntityList);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRunnable.getSiteRepository().save(siteEntity);
            pageEntityList.clear();
        }
    }
}