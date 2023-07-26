package searchengine.services.lemma;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.dao.AllEntityDAO;
import searchengine.dao.LemmaEntityDAO;
import searchengine.dao.PageEntityDAO;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class LemmaCollect {
    private final PageEntityDAO pageEntityDAO;
    private final LemmaEntityDAO lemmaEntityDAO;
    private final static Integer BATCH_SIZE = 10;

    public LemmaCollect(AllEntityDAO allEntityDAO) {
        this.pageEntityDAO = allEntityDAO.getPageEntityDAO();
        this.lemmaEntityDAO = allEntityDAO.getLemmaEntityDAO();
    }

    public void collectLemmas(SiteEntity siteEntity) {
        Slice<PageEntity> slice = pageEntityDAO.getSitePageIdSlice(siteEntity, PageRequest.of(0, BATCH_SIZE));
        List<PageEntity> pageEntityList = slice.getContent();
        pageEntityList.forEach(pageEntity -> {
            long start = System.currentTimeMillis();
            System.out.println("Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
            lemmaEntityDAO.addNewLemma(getLemmaOnPage(pageEntity.getContent()), pageEntity);
            long end = System.currentTimeMillis();
            System.out.println("Time elapsed "  + (end-start) + " ms. Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
        });

        while (slice.hasNext()) {
            slice = pageEntityDAO.getSitePageIdSlice(siteEntity, slice.nextPageable());
            slice.getContent().forEach(pageEntity -> {
                long start = System.currentTimeMillis();
                lemmaEntityDAO.addNewLemma(getLemmaOnPage(pageEntity.getContent()), pageEntity);
                long end = System.currentTimeMillis();
                System.out.println("Time elapsed "  + (end-start) + " ms. Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
            });
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