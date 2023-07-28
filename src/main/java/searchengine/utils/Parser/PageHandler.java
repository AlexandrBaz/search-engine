package searchengine.utils.Parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.dto.parser.Page;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.IndexRepositoryService;
import searchengine.services.LemmaRepositoryService;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.utils.lemma.LemmaFinder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class PageHandler {
    private final static String code4xx5xx = "[45]\\d{2}";

    public SiteRepositoryService siteRepositoryService;
    public PageRepositoryService pageRepositoryService;
    public LemmaRepositoryService lemmaRepositoryService;
    public IndexRepositoryService indexRepositoryService;

    //Rewrite Methods

    public void addUpdatePage(String url, String domain) {
        Page page = parsePage(url,domain);
        long start = System.currentTimeMillis();
        if (!String.valueOf(page.getPageStatusCode()).matches(code4xx5xx)) {
            SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(page.getDomain());
            PageEntity pageEntity = pageRepositoryService.getPageEntity(page.getPath(), siteEntity);
            if (pageRepositoryService.pageEntityIsPresent(page.getPath(), domain)) {
                List<IndexEntity> indexEntityList = indexRepositoryService.getListIndexEntity(pageEntity);
                indexRepositoryService.deleteIndexEntity(indexEntityList);
                lemmaRepositoryService.deleteLemmaOnPage(indexEntityList);
                pageRepositoryService.deletePage(page.getPath(), page.getDomain());
            }
            pageRepositoryService.addPage(page);
            lemmaRepositoryService.addNewLemma(getLemmaOnPage(page.getDocument().outerHtml()), pageRepositoryService.getPageEntity(page.getPath(), siteEntity));
            long end = System.currentTimeMillis();
            System.out.println("Time elapsed "  + (end-start) + " ms. Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
        }
    }

    private Page parsePage(String url, String domain) {
        Page page = new Page();
        Connection.Response response;
        try {
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru-RU) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.11 Safari/534.16")
//                    .referrer("https://www:google.com")
                    .timeout(10000)
                    .ignoreHttpErrors(true)
                    .followRedirects(false)
                    .execute();
            Document document = response.parse();
            page.setPageStatusCode(response.statusCode());
            page.setDocument(document);
            page.setPath(url.replaceAll(domain, "/"));
            page.setDomain(domain);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return page;
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
    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService){
        this.siteRepositoryService = siteRepositoryService;
    }
    @Autowired
    public void setPageRepositoryService(PageRepositoryService pageRepositoryService){
        this.pageRepositoryService = pageRepositoryService;
    }
    @Autowired
    public void setLemmaRepositoryService(LemmaRepositoryService lemmaRepositoryService){
        this.lemmaRepositoryService = lemmaRepositoryService;
    }

    @Autowired
    public void setIndexRepositoryService(IndexRepositoryService indexRepositoryService){
        this.indexRepositoryService = indexRepositoryService;
    }

}
