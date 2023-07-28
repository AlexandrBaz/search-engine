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

public class SiteParser extends RecursiveTask<Boolean> {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ServiceStore serviceStore;
    private final String urlToParse;
    private final String domain;
    private static final Set<String> uniqueUrls = new TreeSet<>();
    private final List<SiteParser> tasks = new ArrayList<>();
    private static final Set<String> allReadyParsed = Collections.synchronizedSet(new HashSet<>());

    public SiteParser(String urlToParse, String domain, ServiceStore serviceStore) {
        this.urlToParse = urlToParse;
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
        Elements elements = parseUrl(urlToParse);
        if (!elements.isEmpty()) {
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (checkUrlForValidAndUnique(childUrl)) {
                    synchronized (uniqueUrls) {
                        uniqueUrls.add(childUrl);
                    }
                    SiteParser siteMapCrawler = new SiteParser(childUrl, domain, serviceStore);
                    siteMapCrawler.fork();
                    tasks.add(siteMapCrawler);
                    System.out.println(uniqueUrls.size() + " uniqueUrls");
                }
            });
        }
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

    private synchronized Elements parseUrl(String url) {
        Page page = new Page();
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
                    allReadyParsed.add(url);
                    System.out.println(allReadyParsed.size() + " allReadyParsed");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println(elements.size() + "elements");
        return elements;
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
