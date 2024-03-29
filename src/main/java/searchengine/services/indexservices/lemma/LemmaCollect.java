package searchengine.services.indexservices.lemma;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.config.AppConfig;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.indexservices.parser.SiteRunnable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
public class LemmaCollect {
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final LemmaFinder lemmaFinder;
    private final AppConfig appConfig;
    Map<String, LemmaEntity> mapLemmaEntity = new ConcurrentHashMap<>();
    public LemmaCollect(@NotNull SiteRunnable siteRunnable) {
        this.lemmaRepository = siteRunnable.getLemmaRepository();
        this.pageRepository = siteRunnable.getPageRepository();
        this.appConfig = siteRunnable.getAppConfig();
        this.lemmaFinder = siteRunnable.getLemmaFinder();
    }

    public void collectMapsLemmas(SiteEntity siteEntity) {
        Slice<PageEntity> slice = pageRepository.findAllBySite(siteEntity, PageRequest.of(0, appConfig.getLemmaSliceSize()));
        int count = 0;
        log.info("getting lemma for slice : {} {}", ++count, siteEntity.getName());
        slice.getContent().stream().parallel().forEach(this::setLemma);
        while (slice.hasNext()) {
            log.info("getting lemma for slice : {} {}", ++count, siteEntity.getName());
            slice = pageRepository.findAllBySite(siteEntity, slice.nextPageable());
            slice.getContent().stream().parallel().forEach(this::setLemma);
        }
        writeToDB(mapLemmaEntity);
    }

    private synchronized void writeToDB(@NotNull Map<String, LemmaEntity> mapLemmaEntity){
        List<LemmaEntity> lemmaEntities = mapLemmaEntity.values().stream().toList();
        mapLemmaEntity.clear();
        lemmaRepository.saveAllAndFlush(lemmaEntities);
    }

    private void setLemma(@NotNull PageEntity pageEntity) {
       Map<String, Integer> lemmaOnPage = lemmaFinder.collectLemmas(pageEntity.getContent());
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
}