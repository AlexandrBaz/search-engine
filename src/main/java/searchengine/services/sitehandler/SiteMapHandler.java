package searchengine.services.sitehandler;

import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveAction;

public class SiteMapHandler extends RecursiveAction {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sitesList;
    private final String urlToParse;
    private final String domain;
    //    Logger logger = LogManager.getLogger(SiteMapHandler.class);
    private final List<SiteMapHandler> tasks = new ArrayList<>();
    private static final Set<String> visitUrl = new TreeSet<>();


    public SiteMapHandler(String urlToParse, String domain, SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList) {
        this.urlToParse = urlToParse;
        this.domain = domain;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
    }

    @Override
    protected void compute() {
        List<SiteMapHandler> allTasks = parseSite();
        invokeAll(allTasks);
    }

    public List<SiteMapHandler> parseSite() {
        Connection.Response response;
        try {
            response = Jsoup.connect(urlToParse)
                    .userAgent(sitesList.getUserAgent())
                    .referrer(sitesList.getReferrer())
                    .execute();
            int urlStatusCode = response.statusCode();
            String code4xx5xx = "[45]\\d{2}";
            if (!String.valueOf(urlStatusCode).matches(code4xx5xx)) {
                getOnPageUrls(response, urlStatusCode);
            }
        } catch (IOException error) {
            error.printStackTrace();
//            logger.error(e);
            setParseError(getSiteFromTable(), error.toString());
        }
        return tasks;
    }

    public void getOnPageUrls(Connection.Response response, int urlStatusCode) {
        try {
            Thread.sleep(300);
            Document doc = response.parse();
            Elements elements = doc.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                synchronized (visitUrl) {
                    String path = childUrl.replaceFirst(domain, "/");
                    if (urlIsValid(childUrl, domain) && !urlIsUnique(path)) {
                        addUrlToTable(urlStatusCode, doc, path);
                        visitUrl.add(childUrl);
                        System.out.println("Size - " + visitUrl.size());
                        SiteMapHandler siteMapHandler = new SiteMapHandler(childUrl, domain, siteRepository, pageRepository,sitesList);
                        siteMapHandler.fork();
                        tasks.add(siteMapHandler);
                    }
                }
            });
        } catch (IOException | InterruptedException error) {
            error.printStackTrace();
            setParseError(getSiteFromTable(), error.toString());
//            logger.error(e);
        }
    }

    public void addUrlToTable(int urlStatusCode, Document doc, String path) {
        Site site = modifySite();
        Page page = new Page();
        page.setCode(urlStatusCode);
        page.setPath(path);
        page.setContent(doc.toString());
        page.setSite(site);
        pageRepository.save(page);
    }

    public boolean urlIsValid(String url, String domain) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[#?]";
        return !url.matches(mediaRegex) && url.contains(domain) && (url.endsWith("/") || url.endsWith("html"));
    }

    public boolean urlIsUnique(String path) {
        return pageRepository.findByPathAndSite(path, getSiteFromTable()).isPresent();
    }

    public Site modifySite() {
        Site modifyedSite = getSiteFromTable();
        modifyedSite.setStatusTime(LocalDateTime.now());
        siteRepository.save(modifyedSite);
        return modifyedSite;
    }

    public void setParseError(Site getSiteFromTable, String error) {
        getSiteFromTable.setStatusTime(LocalDateTime.now());
        getSiteFromTable.setLastError(error);
        siteRepository.save(getSiteFromTable);
    }

    private Site getSiteFromTable() {
        return siteRepository.findByUrl(domain).orElse(null);
    }
}
