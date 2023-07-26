package searchengine.services.sitehandler;

import org.springframework.stereotype.Component;
import searchengine.dao.*;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PageHandler {
    public SiteEntityDAO siteEntityDAO;
    public PageEntityDAO pageEntityDAO;
    public final LemmaEntityDAO lemmaEntityDAO;
    public IndexEntityDAO indexEntityDAO;

    public PageHandler(AllEntityDAO allEntityDAO) {
        this.siteEntityDAO = allEntityDAO.getSiteEntityDAO();
        this.pageEntityDAO = allEntityDAO.getPageEntityDAO();
        this.lemmaEntityDAO = allEntityDAO.getLemmaEntityDAO();
        this.indexEntityDAO = allEntityDAO.getIndexEntityDAO();
    }

    public void addUpdatePage(String url, String domain) {
        ParsedPage parsedPage = new ParsedPage();
        parsedPage.setDomain(domain);
        parsedPage.setUrlToParse(url);
        parsedPage = new JsoupParser().getDocument(parsedPage);
        String code4xx5xx = "[45]\\d{2}";
        long start = System.currentTimeMillis();
        if (!String.valueOf(parsedPage.getPageStatusCode()).matches(code4xx5xx)) {
            parsedPage.setPath(url.replaceAll(domain, "/"));
            SiteEntity siteEntity = siteEntityDAO.getSiteEntity(parsedPage.getDomain());
            PageEntity pageEntity = pageEntityDAO.getPageEntity(parsedPage.getPath(), siteEntity);
            if (pageEntityDAO.pageEntityIsPresent(parsedPage.getPath(), domain)) {
                List<IndexEntity> indexEntityList = new ArrayList<>(indexEntityDAO.getListIndexOnPage(pageEntity));
                indexEntityDAO.deleteIndexOnPage(indexEntityList);
                lemmaEntityDAO.deleteLemmaOnPage(indexEntityList);
                pageEntityDAO.deleteUrl(parsedPage.getPath(), parsedPage.getDomain());
            }
            pageEntityDAO.addUrlToTable(parsedPage);
                lemmaEntityDAO.addNewLemma(getLemmaOnPage(parsedPage.getDocument().outerHtml()), pageEntityDAO.getPageEntity(parsedPage.getPath(), siteEntity));
            long end = System.currentTimeMillis();
            System.out.println("Time elapsed "  + (end-start) + " ms. Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
        }
    }

    private Map<String, Integer> getLemmaOnPage(String pageHtml) {
        Map<String, Integer> lemmaOnPage;
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            lemmaOnPage = lemmaFinder.collectLemmas(pageHtml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lemmaOnPage;
    }

}
