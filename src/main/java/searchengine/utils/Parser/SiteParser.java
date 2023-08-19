package searchengine.utils.Parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.parser.Page;
import searchengine.services.PageRepositoryService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReentrantLock;

public class SiteParser extends RecursiveAction {
    ReentrantLock reentrantLock = new ReentrantLock();
    private final String domain;
    private final String urlToParse;
    private final Set<String> uniqueUrls;
    private final List<SiteParser> tasks = new ArrayList<>();
    private final CopyOnWriteArrayList<Page> pageEntityList;
    private final PageRepositoryService pageRepositoryService;
    private final static Set<String> parsedUrls = new HashSet<>();

    public SiteParser(String urlToParse, String domain, Set<String> uniqueUrls, CopyOnWriteArrayList<Page> pageEntityList, PageRepositoryService pageRepositoryService) {
        this.urlToParse = urlToParse;
        this.domain = domain;
        this.uniqueUrls = uniqueUrls;
        this.pageEntityList =pageEntityList;
        this.pageRepositoryService = pageRepositoryService;
    }

    @Override
    protected void compute() {
        List<SiteParser> allTasks = getNewUrlsForTasks();
        for (SiteParser task : allTasks){
            task.join();
        }
//        invokeAll(allTasks);
    }


//    public List<SiteParser> getNewUrlsForTasks() {
//        if (!parsedUrls.contains(urlToParse)) {
//            try {
////                System.out.println(urlToParse);
//                Thread.sleep(300);
//                Connection.Response response = Jsoup.connect(urlToParse)
//                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
//                        .timeout(20000)
//                    .ignoreHttpErrors(true)
//                        .execute();
//                Document document = response.parse();
//                addToPageList(document, response.statusCode());
//                System.out.println(pageList.size() + " pageList.size() " + uniqueUrls.size() + " uniqueUrls.size()");
//                Elements elements = document.select("a[href]");
//                    elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered(this::checkUrlAndAddTask);
//            } catch (InterruptedException | IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return tasks;
//    }

    public List<SiteParser> getNewUrlsForTasks(){
        Elements elements = parsePageAndReturnElements(urlToParse);
        elements.stream().map(url -> url.attr("abs:href")).forEachOrdered(this::checkUrlAndAddTask);
        System.out.println(parsedUrls.size() + " " + uniqueUrls.size());
        return tasks;
    }

    public Elements parsePageAndReturnElements(String urlToParse){
        Elements elements = new Elements();
        reentrantLock.lock();
        try {
            if (parsedUrls.isEmpty() || !parsedUrls.contains(urlToParse)){
                elements = parsePage(urlToParse);
                return elements;
            }
        }
        finally {
            reentrantLock.unlock();
        }
        return elements;
    }

    public Elements parsePage(String urlToParse){
        Elements elements;
        try {
            Thread.sleep(300);
            Connection.Response response = Jsoup.connect(urlToParse)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20000)
                    .ignoreHttpErrors(true)
                    .execute();
            Document document = response.parse();
            createPageEntity(document, response.statusCode());
            elements = document.select("a[href]");
            parsedUrls.add(urlToParse);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    public void createPageEntity(Document document, int statusCode){
        Page page = new Page();
        page.setPageStatusCode(statusCode);
        page.setDocument(document);
        page.setDomain(domain);
        page.setPath(urlToParse.replaceAll(domain, "/"));
        pageEntityList.add(page);
        if (pageEntityList.size() == 20){
            pageRepositoryService.addListPageEntity(pageEntityList, domain);
            pageEntityList.clear();
        }
    }

//    public void addToPageList(Document document, int statusCode){
//        reentrantLock.lock();
//        try {
//            Page page = new Page();
//            page.setPageStatusCode(statusCode);
//            page.setDocument(document);
//            page.setDomain(domain);
//            page.setPath(urlToParse.replaceAll(domain, "/"));
//            if(pageList.isEmpty() || checkPageListBeforeAdd(urlToParse)){
//                pageList.put(urlToParse, page);
//            }
//        }
//        finally {
//            reentrantLock.unlock();
//        }
//    }
//
//    private boolean checkPageListBeforeAdd(String url){
//        reentrantLock.lock();
//        try {
//            return !pageList.containsKey(url);
//        }
//        finally {
//            reentrantLock.unlock();
//        }
//    }

    private void checkUrlAndAddTask(String url) {
        reentrantLock.lock();
        try {
            if (urlIsValid(url) && !uniqueUrls.contains(url)) {
                uniqueUrls.add(url);
                SiteParser siteParser = new SiteParser(url, domain, uniqueUrls, pageEntityList, pageRepositoryService);
                siteParser.fork();
                tasks.add(siteParser);
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    private boolean urlIsValid(@NotNull String url) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.startsWith(domain)
                && (url.endsWith("/")
                || url.endsWith("html"));
    }
}