package searchengine.services.indexservices.lemma;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.config.AppConfig;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.indexservices.parser.SiteRunnable;
import searchengine.services.reposervices.IndexRepositoryService;
import searchengine.services.reposervices.LemmaRepositoryService;
import searchengine.services.reposervices.PageRepositoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Log4j2
public class IndexCollect {
    private final LemmaRepositoryService lemmaRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final LemmaFinder lemmaFinder;
    private final List<IndexEntity> indexEntityList = new ArrayList<>();
    private final SiteEntity siteEntity;
    private final AppConfig appConfig;

    public IndexCollect(@NotNull SiteRunnable siteRunnable) {
        this.siteEntity = siteRunnable.getSiteEntity();
        this.appConfig = siteRunnable.getAppConfig();
        this.pageRepositoryService = siteRunnable.getPageRepositoryService();
        this.lemmaRepositoryService = siteRunnable.getLemmaRepositoryService();
        this.indexRepositoryService = siteRunnable.getIndexRepositoryService();
        this.lemmaFinder = siteRunnable.getLemmaFinder();
    }

    public void lemmaRankBySite() {
        List<LemmaEntity> lemmaEntityList = lemmaRepositoryService.getAllLemmaEntityBySiteEntity(siteEntity);
        Slice<PageEntity> slice = pageRepositoryService.getSliceOfPages(siteEntity, PageRequest.of(0, appConfig.getIndexSliceSize()));
        int count = 0;
        log.info("getting index for slice : {} {}", ++count, siteEntity.getName());
        slice.getContent().stream().parallel()
                .forEach(pageEntity -> {
                    Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                    collectIndexLemma(lemmaRankFromPage, pageEntity, lemmaEntityList);
                });
        while (slice.hasNext()) {
            log.info("getting index for slice : {} {}", ++count, siteEntity.getName());
            slice = pageRepositoryService.getSliceOfPages(siteEntity, slice.nextPageable());
            slice.getContent().stream().parallel()
                    .forEach(pageEntity -> {
                        Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                        collectIndexLemma(lemmaRankFromPage, pageEntity, lemmaEntityList);
                    });
        }
        indexRepositoryService.addIndexEntityList(indexEntityList);
    }

    private void collectIndexLemma(@NotNull Map<String, Float> partIndexLemma,
                                   PageEntity pageEntity,
                                   List<LemmaEntity> lemmaEntityList) {
        partIndexLemma.forEach((lemma, rank) -> {
            LemmaEntity lemmaEntity = lemmaEntityList.stream()
                    .parallel()
                    .filter(lem -> lem.getLemma().equals(lemma))
                    .findFirst()
                    .orElse(null);
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setLemmaRank(rank);
            indexEntity.setPage(pageEntity);
            indexEntity.setLemma(lemmaEntity);
            addToListAndWriteToDB(indexEntity);
        });
    }

    private synchronized void addToListAndWriteToDB(IndexEntity indexEntity){
        indexEntityList.add(indexEntity);
        if(indexEntityList.size() == appConfig.getIndexWriteSize()) {
            long start = System.currentTimeMillis();
            List<IndexEntity> writeIndexEntityList = new ArrayList<>(indexEntityList);
            indexEntityList.clear();
            indexRepositoryService.addIndexEntityList(writeIndexEntityList);
            writeIndexEntityList.clear();
            log.info("Total time write to DB {}, {} ms", indexEntityList.size(), (System.currentTimeMillis() - start));
        }
    }

}