package searchengine.services.sitehandler;

import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveAction;

@Scope("prototype")
public class SiteMapHandler extends RecursiveAction {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String urlToParse;
    private final String domain;
//    Logger logger = LogManager.getLogger(SiteMapHandler.class);
    private final List<SiteMapHandler> tasks = new ArrayList<>();

    public SiteMapHandler(String childUrl, SiteRepository siteRepository, PageRepository pageRepository) {
        this.urlToParse = childUrl;
        this.domain = urlToParse;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    public SiteMapHandler(String urlToParse, String domain, SiteRepository siteRepository, PageRepository pageRepository) {
        this.urlToParse = urlToParse;
        this.domain = domain;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    protected void compute() {
        List<SiteMapHandler> allTasks = new ArrayList<>(parseSite());
        invokeAll(allTasks);
    }

    public List<SiteMapHandler> parseSite() {
        Connection.Response response;
        try {
            response = Jsoup.connect(urlToParse)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .execute();
            int urlStatusCode = response.statusCode();
            String code4xx5xx = "[45]\\d{2}";
            if (!String.valueOf(urlStatusCode).matches(code4xx5xx)) {
                getOnPageUrls(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
//            logger.error(e);
        }
        return tasks;
    }

    public void getOnPageUrls(Connection.Response response) {
        try {
            Thread.sleep(300);
            Document doc = response.parse();
            Elements elements = doc.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                synchronized (pageRepository) {
                    addUrlToTable(childUrl, response, doc);
                    }
                    SiteMapHandler siteMapHandler = new SiteMapHandler(childUrl, domain, siteRepository, pageRepository);
                    siteMapHandler.fork();
                    tasks.add(siteMapHandler);
                    System.out.println("in progress");
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
//            logger.error(e);
        }
    }

    public void addUrlToTable(String childUrl, Connection.Response response, Document doc) {
        String path = urlToParse.replaceFirst(domain, "/");
        Site siteInTable = siteRepository.findByUrl(domain).orElse(null);
        if (urlIsValid(childUrl, domain) && !urlIsUnique(path, siteInTable)) {
            assert siteInTable != null;
            Site site = modifySite(siteInTable);
            Page page = new Page();
            page.setCode(response.statusCode());
            page.setPath(path);
            page.setContent(doc.toString());
            page.setSite(site);
            pageRepository.save(page);
        }
    }

    public boolean urlIsValid(String url, String domain) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[#?]";
        return !url.matches(mediaRegex) && url.contains(domain) && (url.endsWith("/") || url.endsWith("html"));
    }

    public boolean urlIsUnique(String path, Site siteInTable) {
        return pageRepository.findByPathAndSite(path, siteInTable).isPresent();
    }

    public Site modifySite(Site siteInTable) {
        siteInTable.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteInTable);
        return siteInTable;
    }
}
