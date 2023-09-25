package searchengine.services.indexServices.parser;

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
    private final Set<String> uniqUrl;
    private final Set<String> pageEntityAlreadyParsed;
    private final SiteRunnable siteRunnable;
    private final ReentrantLock lock = new ReentrantLock();
    private final static int BATCH_PARSE = 100;
    private final static String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; ru-RU) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.11 Safari/534.16";
    private final static int TIME_OUT = 100000;
    private final static boolean IGNORE_HTTP_ERRORS = true;
    private final static int THREAD_SLEEP = 500;
    private final static String MEDIA_REGEX = "(.*/)*.+\\\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";


    public SiteParserBatch(List<String> listUrlsToParse, @NotNull SiteRunnable siteRunnable) {
        this.listUrlsToParse = listUrlsToParse;
        this.siteRunnable = siteRunnable;
        this.uniqUrl = siteRunnable.getUniqUrl();
        this.pageEntityAlreadyParsed = siteRunnable.getPageEntityAlreadyParsed();
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
                    SiteParserBatch siteParserBatch = new SiteParserBatch(nextListUrToParse, siteRunnable);
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
        return !url.matches(MEDIA_REGEX)
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
                        .userAgent(USER_AGENT)
                        .timeout(TIME_OUT)
                        .ignoreHttpErrors(IGNORE_HTTP_ERRORS)
                        .execute();
                Document document = response.parse();
                createPage(document, url, response.statusCode());
                Elements elements = document.select("a[href]");
                urlList = elements.stream().map(absUrl -> absUrl.attr("abs:href")).toList();
            } catch (IOException e) {
                siteRunnable.getSiteRepositoryService().setParseError(siteRunnable.getSiteEntity(), e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return urlList;
    }

    private void threadSleep() {
        try {
            Thread.sleep(THREAD_SLEEP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void createPage(@NotNull Document document, @NotNull String url, int statusCode) {
        ConcurrentHashMap<String, PageEntity> pageEntityMap = siteRunnable.getPageEntityMap();
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

    private void checkAndWrite(@NotNull ConcurrentHashMap<String, PageEntity> pageEntityMap, SiteEntity siteEntity) {
        if (pageEntityMap.size() == BATCH_PARSE) {
            log.info("Write batch toDB: total uniqUrl -> " + uniqUrl.size() + " for domain -> " + siteEntity.getName());
            List<PageEntity> pageEntityList = new ArrayList<>(pageEntityMap.values().stream().toList());
            pageEntityMap.clear();
            siteRunnable.getPageRepositoryService().addListPageEntity(pageEntityList);
            siteRunnable.getSiteRepositoryService().updateSite(siteEntity);
            pageEntityList.clear();
        }
    }
}
