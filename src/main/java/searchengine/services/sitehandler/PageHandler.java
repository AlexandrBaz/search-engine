package searchengine.services.sitehandler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.services.PageEntityDAO;
import searchengine.services.SiteEntityDAO;

import java.io.IOException;

@Component
public class PageHandler {
    public SiteEntityDAO siteEntityDAO;
    public PageEntityDAO pageEntityDAO;

    public PageHandler(SiteEntityDAO siteEntityDAO, PageEntityDAO pageEntityDAO){
        this.siteEntityDAO = siteEntityDAO;
        this.pageEntityDAO = pageEntityDAO;
    }

    public void addUpdatePage(String url, String domain, SitesList sitesList){
        Connection.Response response;
        String path = url.replaceFirst(domain, "/");
        try {
            response = Jsoup.connect(url)
                    .userAgent(sitesList.getUserAgent())
                    .referrer(sitesList.getReferrer())
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int statusCode = response.statusCode();
        String code4xx5xx = "[45]\\d{2}";
        if (!String.valueOf(statusCode).matches(code4xx5xx)){
            try {
                Document doc = response.parse();
                if(pageEntityDAO.pageEntityIsPresent(path,domain)){
                    pageEntityDAO.upDatePageEntity(path, domain, statusCode, doc);
                } else {
                    pageEntityDAO.addUrlToTable(statusCode, doc, path, domain);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
