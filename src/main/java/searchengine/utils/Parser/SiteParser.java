package searchengine.utils.Parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.parser.Page;
import searchengine.utils.ServiceStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReentrantLock;

public class SiteParser extends RecursiveAction {
    ReentrantLock reentrantLock = new ReentrantLock();
    private final String domain;
    private final String urlToParse;
    private final TreeSet<String> uniqueUrls;
    private final List<SiteParser> tasks = new ArrayList<>();
    private final TreeMap<String,Page> pageList;

    public SiteParser(String urlToParse, String domain, TreeSet<String> uniqueUrls, TreeMap<String, Page> pageList) {
        this.urlToParse = urlToParse;
        this.domain = domain;
        this.uniqueUrls = uniqueUrls;
        this.pageList =pageList;
    }

    @Override
    protected void compute() {
        List<SiteParser> allTasks = getNewUrlsForTasks();
        invokeAll(allTasks);
    }


    public List<SiteParser> getNewUrlsForTasks() {
        if (!pageList.containsKey(urlToParse)) {
            try {
                System.out.println(urlToParse);
                Thread.sleep(300);
                Connection.Response response = Jsoup.connect(urlToParse)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .timeout(20000)
                    .ignoreHttpErrors(true)
//                        .method(Connection.Method.GET)
                        .execute();
                Document document = response.parse();
                addToPageList(document, response.statusCode());
                System.out.println(pageList.size() + " pageList.size() " + uniqueUrls.size() + " uniqueUrls.size()");
                Elements elements = document.select("a[href]");
                    elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered(this::checkUrlAndAddTask);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        return tasks;
    }

    public void addToPageList(Document document, int statusCode){
        reentrantLock.lock();
        try {
            Page page = new Page();
            page.setPageStatusCode(statusCode);
            page.setDocument(document);
            page.setDomain(domain);
            page.setPath(urlToParse.replaceAll(domain, "/"));
            if(pageList.isEmpty() || checkPageListBeforeAdd(urlToParse)){
                pageList.put(urlToParse, page);
            }
        }
        finally {
            reentrantLock.unlock();
        }
    }

    private boolean checkPageListBeforeAdd(String url){
        reentrantLock.lock();
        try {
            return !pageList.containsKey(url);
        }
        finally {
            reentrantLock.unlock();
        }
    }

    private void checkUrlAndAddTask(String url) {
        reentrantLock.lock();
        try {
            if (urlIsValid(url) && urlIsUnique(url)) {
                uniqueUrls.add(url);
                SiteParser siteParser = new SiteParser(url, domain, uniqueUrls, pageList);
                siteParser.fork();
                tasks.add(siteParser);
            }
        } finally {
            reentrantLock.unlock();
        }
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