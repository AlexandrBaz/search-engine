package searchengine.utils.Parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.dto.parser.Page;
import searchengine.utils.ServiceStore;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

@Component
public class SiteMapHandler extends RecursiveTask<Boolean> {


    private final ServiceStore serviceStore;
    private final String domain;
    private final String nextUrlToParse;
    private final List<SiteMapHandler> tasks = new ArrayList<>();
    private static final List<String> noChecked = new ArrayList<>();
    private static final Set<String> uniqueUrls = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> allReadyParsed = Collections.synchronizedSet(new HashSet<>());

    //    Logger logger = LogManager.getLogger(SiteMapHandler.class);

    public SiteMapHandler(String domain, String nextUrlToParse, ServiceStore serviceStore) {
        this.domain = domain;
        this.nextUrlToParse = nextUrlToParse;
        this.serviceStore = serviceStore;

    }

    @Autowired
    public SiteMapHandler(ParsedPage parsedPage, ServiceStore serviceStore) {
        this.domain = parsedPage.getDomain();
        this.nextUrlToParse = parsedPage.getDomain();
        this.serviceStore = serviceStore;
    }

    @Override
    protected Boolean  compute() {
        if (!Thread.currentThread().isInterrupted()) {
            List<SiteMapHandler> allTasks = getTasks();
//            invokeAll(allTasks);
            for (SiteMapHandler task : allTasks) {
                task.join();
            }
        } else {
            System.out.println("shutdown");
        }
        return true;
    }

    public List<SiteMapHandler> getNewUrlsForTasks() {
        try {
            Thread.sleep(300);
            Document document = Jsoup.connect(nextUrlToParse).ignoreHttpErrors(true).get();
            Elements elements = document.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (urlIsValid(childUrl, domain) && allReadyParsed.stream().noneMatch(s -> s.equals(childUrl))) {
                        synchronized (uniqueUrls) {
                            uniqueUrls.add(childUrl);

                        }
                    synchronized (noChecked) {
                        noChecked.add(childUrl);
                    }
                        SiteMapHandler siteMapCrawler = new SiteMapHandler(domain, childUrl, serviceStore);
                        siteMapCrawler.fork();
                        tasks.add(siteMapCrawler);
                        System.out.println("in progress uniqueUrls " + uniqueUrls.size());
                    System.out.println("in progress noChecked " + noChecked.size());
                    }
            });
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<SiteMapHandler> getTasks() {
        Elements elements = parseUrl(nextUrlToParse);
        elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
            if (urlIsValid(childUrl, domain) && allReadyParsed.stream().noneMatch(s -> s.equals(childUrl))) {

                synchronized (tasks){
                    SiteMapHandler siteMapHandler = new SiteMapHandler(domain, childUrl, serviceStore);
                    siteMapHandler.fork();
                    uniqueUrls.add(childUrl);
                    tasks.add(siteMapHandler);
                    System.out.println(uniqueUrls.size() + " uniqueUrls");
                }
            }
        });
        return tasks;
    }

    private synchronized Elements parseUrl(String url) {
        Page page = new Page();
        Elements elements = new Elements();
        if (urlIsValid(url, domain) && allReadyParsed.stream().noneMatch(s -> s.equals(url))) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            Connection.Response response;
            try {
                response = Jsoup.connect(nextUrlToParse)
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
        return elements;
    }

    private synchronized void createButch(Page page) {
        serviceStore.getPageRepositoryService().addPage(page);
        System.out.println(uniqueUrls.size() + " add -> " + page.getPath() + " " + Thread.currentThread().getName());
    }

    private Boolean checkUrlBeforeParse(String url) {
        return uniqueUrls.stream().noneMatch(s -> s.equals(url));
    }

    private synchronized Boolean checkUrlFromParsedPage(String url, String domain) {
        if (urlIsValid(url, domain) && checkUrlBeforeParse(url)) {
            uniqueUrls.add(url);
            return true;
        }
        return false;
    }

    private boolean urlIsValid(String url, String domain) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.contains(domain)
                && (url.endsWith("/")
                || url.endsWith("html"));
    }
}