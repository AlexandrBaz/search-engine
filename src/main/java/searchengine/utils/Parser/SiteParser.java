package searchengine.utils.Parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.parser.Page;
import searchengine.utils.ServiceStore;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class SiteParser extends RecursiveTask<Boolean> {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ServiceStore serviceStore;
    private final List<String> listUrlToParse;
    private final String domain;
    private static final Set<String> uniqueUrls = new TreeSet<>();
    private final List<SiteParser> tasks = new ArrayList<>();
    private static final Set<String> allReadyParsed = Collections.synchronizedSet(new HashSet<>());

    public SiteParser(List<String> listUrlToParse, String domain, ServiceStore serviceStore) {
        this.listUrlToParse = listUrlToParse;
        this.domain = domain;
        this.serviceStore = serviceStore;
    }

    @Override
    protected Boolean compute() {
        List<SiteParser> allTasks = getNewUrlsForTasks();
        for (SiteParser task : allTasks) {
            task.join();
        }
        return true;
    }

    public List<SiteParser> getNewUrlsForTasks() {
        listUrlToParse.forEach((childUrl) -> {
                if (checkUrlForValidAndUnique(childUrl)) {
                    List<String> listUrlToParse = getUniqueUrlList(childUrl);
                    if(!listUrlToParse.isEmpty()) {
                        SiteParser siteMapCrawler = new SiteParser(listUrlToParse, domain, serviceStore);
                        siteMapCrawler.fork();
                        tasks.add(siteMapCrawler);
                    }
                    System.out.println(uniqueUrls.size() + " uniqueUrls");
                }
            });
        return tasks;
    }

    private boolean checkUrlForValidAndUnique(String url) {
        lock.readLock().lock();
        try {
            if (urlIsValid(url) && urlIsUnique(url)) {
                return true;
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    private List<String> getUniqueUrlList(String url){
        lock.writeLock().lock();
        List<String> uniqueUrlList;
        try {
            uniqueUrlList = parseUrl(url);
            uniqueUrls.add(url);
        } finally {
            lock.writeLock().unlock();
        }
        return uniqueUrlList;
    }

    private synchronized List<String> parseUrl(String url) {
        Page page = new Page();
        List<String> uniqueUrlList = new ArrayList<>();
        Elements elements = new Elements();
        if (url.contains(domain) && !allReadyParsed.contains(url)) {
            if (!url.matches("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]") && !url.contains("?") && !url.contains("#")) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                Connection.Response response;
                try {
                    response = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
//                    .referrer("http://www:google.com")
                            .timeout(10000)
                            .followRedirects(false)
                            .execute();
                    Document document = response.parse();
//                page.setDomain(domain);
//                page.setPageStatusCode(response.statusCode());
//                page.setPath(url.replaceAll(domain, "/"));
//                page.setDocument(document);
//                createButch(page);
                    elements = document.select("a[href]");
                    if(!elements.isEmpty()) {
                        uniqueUrlList = elements.stream()
                                .map(element -> element.attr("abs:href"))
                                .filter(urlChecking -> !uniqueUrls.contains(urlChecking))
                                .collect(Collectors.toList());
                        allReadyParsed.add(url);
                    }
                    System.out.println(allReadyParsed.size() + " allReadyParsed");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return uniqueUrlList;
    }

    public static boolean isUniqueUrl(String url) {
        return getUniqueUrls().contains(url);
    }

    public static Set<String> getUniqueUrls() {
        return uniqueUrls;
    }

    public static int getLevel(String url) {
        return url.replaceAll("[^/]", "").length() - 2;
    }

    private Boolean urlIsUnique(String url) {
        return !uniqueUrls.contains(url);
    }

    private boolean urlIsValid(String url) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.contains(domain)
                && (url.endsWith("/")
                || url.endsWith("html"));
    }
}
