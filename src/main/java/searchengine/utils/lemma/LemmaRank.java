package searchengine.utils.lemma;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.*;
import searchengine.utils.parser.SiteRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Component
@Getter
@Log4j2
public class LemmaRank {
    @Value("${index-batch}")
    private int BATCH_SIZE = 100;
    @Value("${batch.index-write}")
    private int INDEX_WRITE_SIZE = 200000;
    private final SiteRepositoryService siteRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final IndexServiceAsync indexServiceAsync;
    private final List<IndexEntity> sliceLemmaRank = new ArrayList<>();
    private final List<IndexEntity> indexEntityList = new ArrayList<>();
    private final LemmaFinder lemmaFinder;
    private final Boolean parseActive;
    private final SiteRunnable siteRunnable;

    public LemmaRank(@NotNull SiteRunnable siteRunnable) {
        this.siteRunnable = siteRunnable;
        this.pageRepositoryService = siteRunnable.getPageRepositoryService();
        this.siteRepositoryService = siteRunnable.getSiteRepositoryService();
        this.lemmaRepositoryService = siteRunnable.getLemmaRepositoryService();
        this.indexRepositoryService = siteRunnable.getIndexRepositoryService();
        this.indexServiceAsync = siteRunnable.getIndexServiceAsync();
        this.lemmaFinder = getLemmaFinderInstance();
        this.parseActive = siteRunnable.getParseActive();
    }



    public void lemmaRankBySite() {
        SiteEntity siteEntity = siteRunnable.getSiteEntity();
        List<LemmaEntity> lemmaEntityList = lemmaRepositoryService.getAllLemmaEntityBySiteEntity(siteEntity);
        Slice<PageEntity> slice = pageRepositoryService.getSliceOfPages(siteEntity, PageRequest.of(0, BATCH_SIZE));
        slice.getContent().stream().parallel()
                .forEach(pageEntity -> {
                    Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                    collectIndexLemma(lemmaRankFromPage, pageEntity, lemmaEntityList);
                });
        indexRepositoryService.addIndexEntityList(sliceLemmaRank);
        while (slice.hasNext()) {
            sliceLemmaRank.clear();
            slice = pageRepositoryService.getSliceOfPages(siteEntity, slice.nextPageable());
            slice.getContent().stream().parallel()
                    .forEach(pageEntity -> {
                        Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                        collectIndexLemma(lemmaRankFromPage, pageEntity, lemmaEntityList);
                    });
            indexRepositoryService.addIndexEntityList(sliceLemmaRank);
        }
        indexRepositoryService.addIndexEntityList(indexEntityList);
    }

    private void collectIndexLemma(@NotNull Map<String, Float> partIndexLemma,
                                   PageEntity pageEntity,
                                   List<LemmaEntity> lemmaEntityList) {
        partIndexLemma.forEach((lemma, rank) -> {
            LemmaEntity lemmaEntity = lemmaEntityList.stream().parallel().filter(lem -> lem.getLemma().equals(lemma)).findFirst().orElse(null);
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setLemmaRank(rank);
            indexEntity.setPage(pageEntity);
            indexEntity.setLemma(lemmaEntity);
            addToListAndWriteToDB(indexEntity);
        });
    }

    private synchronized void addToListAndWriteToDB(IndexEntity indexEntity){
        indexEntityList.add(indexEntity);
        if(indexEntityList.size() == INDEX_WRITE_SIZE) {
            long start = System.currentTimeMillis();
            List<IndexEntity> writeIndexEntityList = new ArrayList<>(indexEntityList);
            indexEntityList.clear();
            indexRepositoryService.addIndexEntityList(writeIndexEntityList);
            writeIndexEntityList.clear();
            log.info("Время на запись " + INDEX_WRITE_SIZE + " " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    private LemmaFinder getLemmaFinderInstance() {
        LemmaFinder lemmaFinder;
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lemmaFinder;
    }

}