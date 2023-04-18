package searchengine.services.sitehandler;

import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.services.PageEntityDAO;
import searchengine.services.SiteEntityDAO;

import java.util.*;
import java.util.concurrent.RecursiveAction;

@Component

public class SiteMapHandler extends RecursiveAction {
    private final SitesList sitesList;
    private final String urlToParse;
    private final String domain;

    private final SiteEntityDAO siteEntityDAO;
    private final PageEntityDAO pageEntityDAO;

    //    Logger logger = LogManager.getLogger(SiteMapHandler.class);
    private final List<SiteMapHandler> tasks = new ArrayList<>();

    public SiteMapHandler(SiteToCrawl siteToCrawl, SitesList sitesList, SiteEntityDAO siteEntityDAO, PageEntityDAO pageEntityDAO) {
        this.urlToParse = siteToCrawl.getUrlToCrawl();
        this.domain = siteToCrawl.getDomain();
        this.sitesList = sitesList;
        this.siteEntityDAO = siteEntityDAO;
        this.pageEntityDAO = pageEntityDAO;
    }

    @Override
    protected void compute(){
        if (!Thread.currentThread().isInterrupted()) {
            List<SiteMapHandler> allTasks = parseSite();
            invokeAll(allTasks);
        } else {
            System.out.println("shutdown");
        }
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
            Thread.currentThread().interrupt();
            error.printStackTrace();
//            logger.error(e);
//            setParseError(getSiteFromTable(), error.toString());
        }
        return tasks;
    }

    public void getOnPageUrls(Connection.Response response, int urlStatusCode) {

        try {
            Thread.sleep(300);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            error.printStackTrace();
//            setParseError(getSiteFromTable(), error.toString());
//            logger.error(e);
        }
        try {
            Document doc = response.parse();
            Elements elements = doc.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                String path = childUrl.replaceFirst(domain, "/");
                if (urlIsValid(childUrl, domain) && !pageEntityDAO.urlIsUnique(path, domain)) {
                    pageEntityDAO.addUrlToTable(urlStatusCode, doc, path, domain);
                    SiteToCrawl siteToCrawl = new SiteToCrawl();
                    siteToCrawl.setUrlToCrawl(childUrl);
                    siteToCrawl.setDomain(domain);
                    SiteMapHandler siteMapHandler = new SiteMapHandler(siteToCrawl, sitesList, siteEntityDAO, pageEntityDAO);
                    siteMapHandler.fork();
                    tasks.add(siteMapHandler);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean urlIsValid(String url, String domain) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[#?]";
        return !url.matches(mediaRegex) && url.contains(domain) && (url.endsWith("/") || url.endsWith("html"));
    }
}