package searchengine.services.sitehandler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.dao.AllEntityDAO;
import searchengine.dao.PageEntityDAO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;

@Component
public class SiteMapHandler extends RecursiveAction {
    private final PageEntityDAO pageEntityDAO;
    private final AllEntityDAO allEntityDAO;

    private String domain;
    private String nextUrlToParse;
    private ParsedPage parsedPage;
    private final List<SiteMapHandler> tasks = new ArrayList<>();

    private static final Set<String> uniqueUrls = Collections.synchronizedSet(new HashSet<>());

    //    Logger logger = LogManager.getLogger(SiteMapHandler.class);

    public SiteMapHandler(String domain, String nextUrlToParse, AllEntityDAO allEntityDAO) {
        this.domain = domain;
        this.nextUrlToParse = nextUrlToParse;
        this.allEntityDAO = allEntityDAO;
        this.pageEntityDAO = allEntityDAO.getPageEntityDAO();

    }

    @Autowired
    public SiteMapHandler(ParsedPage parsedPage, AllEntityDAO allEntityDAO) {
        this.parsedPage = parsedPage;
        this.allEntityDAO = allEntityDAO;
        this.pageEntityDAO = allEntityDAO.getPageEntityDAO();
        this.domain = parsedPage.getDomain();
        this.nextUrlToParse = parsedPage.getDomain();
    }

    @Override
    protected void compute() {
        if (!Thread.currentThread().isInterrupted()) {
            List<SiteMapHandler> allTasks = getTasks();
            invokeAll(allTasks);
        } else {
            System.out.println("shutdown");
        }
    }

    public List<SiteMapHandler> getTasks() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
            ////            setParseError(getSiteFromTable(), error.toString());
        }
        Connection.Response response;
        try {
            response = Jsoup.connect(nextUrlToParse)
                    .userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru-RU) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.11 Safari/534.16")
//                    .referrer("https://www:google.com")
                    .timeout(10000)
                    .ignoreHttpErrors(true)
                    .followRedirects(false)
                    .execute();
            Document document = response.parse();
            Elements elements = document.select("a[href]");


            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (checkUrlFromParsedPage(childUrl, domain)) {
                    SiteMapHandler siteMapHandler = new SiteMapHandler(domain, childUrl, allEntityDAO);
                    siteMapHandler.fork();
                    ParsedPage page = new ParsedPage();
                    page.setDocument(document);
                    page.setPageStatusCode(response.statusCode());
                    page.setPath(childUrl.replaceAll(domain, "/"));
                    page.setDomain(domain);
                    page.setUrlToParse(childUrl);
                    pageEntityDAO.addUrlToTable(page);
                    tasks.add(siteMapHandler);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    private Boolean checkUrlBeforeParse(String url) {
        return !uniqueUrls.contains(url);
    }

    private synchronized Boolean checkUrlFromParsedPage(String url, String domain) {
        if (urlIsValid(url, domain) && checkUrlBeforeParse(url)) {
            uniqueUrls.add(url);
            System.out.println(uniqueUrls.size() + " add -> " + url + " " + Thread.currentThread().getName());
            return true;
        }
        return false;
    }

    private synchronized ParsedPage createNewParsedPage(String url, ParsedPage currentPage) {
        ParsedPage newPage = new ParsedPage();
        newPage.setDomain(currentPage.getDomain());
        newPage.setUrlToParse(url);
        newPage = new JsoupParser().getDocument(newPage);
        pageEntityDAO.addUrlToTable(newPage);
        System.out.println("write to DB -> " + newPage.getPath() + " " + newPage.getUrlToParse());
        return newPage;
    }

    private boolean urlIsValid(String url, String domain) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.contains(domain)
                && (url.endsWith("/")
                || url.endsWith("html"));
    }

    private void threadSleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
            ////            setParseError(getSiteFromTable(), error.toString());
        }
    }
}