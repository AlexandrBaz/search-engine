package searchengine.utils.lemma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.LemmaRepositoryService;
import searchengine.services.PageRepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class LemmaCollect {
    private final static Integer BATCH_SIZE = 10;
    private PageRepositoryService pageRepositoryService;
    private LemmaRepositoryService lemmaRepositoryService;


    public void collectLemmas(SiteEntity siteEntity) {
        Slice<PageEntity> slice = pageRepositoryService.getSliceOfPages(siteEntity, PageRequest.of(0, BATCH_SIZE));
        List<PageEntity> pageEntityList = slice.getContent();
        pageEntityList.forEach(pageEntity -> {
            long start = System.currentTimeMillis();
            System.out.println("Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
            lemmaRepositoryService.addNewLemma(getLemmaOnPage(pageEntity.getContent()), pageEntity);
            long end = System.currentTimeMillis();
            System.out.println("Time elapsed "  + (end-start) + " ms. Collect lemma for url " + pageEntity.getPath() + " where id is " + pageEntity.getId());
        });

        while (slice.hasNext()) {
            slice = pageRepositoryService.getSliceOfPages(siteEntity, slice.nextPageable());
            slice.getContent().forEach(pageEntity -> {
                long start = System.currentTimeMillis();
                lemmaRepositoryService.addNewLemma(getLemmaOnPage(pageEntity.getContent()), pageEntity);
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

    @Autowired
    public void setPageRepositoryService(PageRepositoryService pageRepositoryService){
        this.pageRepositoryService = pageRepositoryService;
    }

    @Autowired
    public void setLemmaRepositoryService(LemmaRepositoryService lemmaRepositoryService){
        this.lemmaRepositoryService = lemmaRepositoryService;
    }

}