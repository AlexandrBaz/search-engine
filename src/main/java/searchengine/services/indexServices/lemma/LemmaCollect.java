package searchengine.services.indexServices.lemma;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.repoServices.LemmaRepositoryService;
import searchengine.services.repoServices.PageRepositoryService;
import searchengine.services.indexServices.parser.SiteRunnable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
public class LemmaCollect {
    private final static Integer BATCH_SIZE = 100;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;

    ConcurrentHashMap<String, LemmaEntity> mapLemmaEntity = new ConcurrentHashMap<>();
    public LemmaCollect(@NotNull SiteRunnable siteRunnable) {
        this.lemmaRepositoryService = siteRunnable.getLemmaRepositoryService();
        this.pageRepositoryService = siteRunnable.getPageRepositoryService();
    }

    public void collectMapsLemmas(SiteEntity siteEntity) {
        long start = System.currentTimeMillis();
        Slice<PageEntity> slice = pageRepositoryService.getSliceOfPages(siteEntity, PageRequest.of(0, BATCH_SIZE));
        List<PageEntity> pageEntityList = slice.getContent();
        pageEntityList.stream().parallel().forEach(this::setLemma);
        int count = 1;
        while (slice.hasNext()) {
            count++;
            log.info("getting lemma for slice : " + count + " " + siteEntity.getName());
            slice = pageRepositoryService.getSliceOfPages(siteEntity, slice.nextPageable());
            slice.getContent().stream().parallel().forEach(this::setLemma);

        }
        writeToDB(mapLemmaEntity);
        long end = System.currentTimeMillis();
        log.info("Time elapsed " + (end - start) + " ms. Collect lemma");

    }

    private synchronized void writeToDB(@NotNull ConcurrentHashMap<String, LemmaEntity> mapLemmaEntity){
        List<LemmaEntity> lemmaEntities = mapLemmaEntity.values().stream().toList();
        mapLemmaEntity.clear();
        lemmaRepositoryService.addLemmaEntityList(lemmaEntities);
    }

    private void setLemma(@NotNull PageEntity pageEntity) {
        ConcurrentHashMap<String, Integer> lemmaOnPage = getLemmaOnPage(pageEntity.getContent());
        lemmaOnPage.forEach((lemma, frequency) -> {
            LemmaEntity lemmaEntity = new LemmaEntity();
            if (!mapLemmaEntity.containsKey(lemma)) {
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(1);
                lemmaEntity.setSite(pageEntity.getSite());
                mapLemmaEntity.put(lemma, lemmaEntity);
            } else {
                LemmaEntity lemmaEntityIsPresent = mapLemmaEntity.get(lemma);
                lemmaEntityIsPresent.setFrequency(lemmaEntityIsPresent.getFrequency() + 1);
            }
        });
    }

    private ConcurrentHashMap<String, Integer> getLemmaOnPage(String pageHtml) {
        ConcurrentHashMap<String, Integer> lemmaOnPage;
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            lemmaOnPage = lemmaFinder.collectLemmas(pageHtml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lemmaOnPage;
    }
}