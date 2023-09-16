package searchengine.utils.lemma;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Log4j2
public class LemmaRank {
    private final static Integer BATCH_SIZE = 20;
    private final SiteRepositoryService siteRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final IndexServiceAsync indexServiceAsync;
    private final List<IndexEntity> sliceLemmaRank = new ArrayList<>();
    private final List<IndexEntity> indexEntityList = new ArrayList<>();

    public LemmaRank(@NotNull SiteRunnable siteRunnable) {
        this.pageRepositoryService = siteRunnable.getPageRepositoryService();
        this.siteRepositoryService = siteRunnable.getSiteRepositoryService();
        this.lemmaRepositoryService = siteRunnable.getLemmaRepositoryService();
        this.indexRepositoryService = siteRunnable.getIndexRepositoryService();
        this.indexServiceAsync = siteRunnable.getIndexServiceAsync();
    }

    public void lemmaRankBySite(String domain) {
        LemmaFinder lemmaFinder;
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
        Slice<PageEntity> slice = pageRepositoryService.getSliceOfPages(siteEntity, PageRequest.of(0, BATCH_SIZE));
        AtomicInteger count = new AtomicInteger(1);
        slice.getContent().parallelStream().unordered()
                .forEach(pageEntity -> {
                    Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                    collectIndexLemma(lemmaRankFromPage, pageEntity, siteEntity);
                    lemmaRankFromPage.clear();
                });
        writeIndexRankToDb();
        while (slice.hasNext()) {
            count.getAndIncrement();
            log.info("getting index for slice : " + count + " " + siteEntity.getName());
            slice = pageRepositoryService.getSliceOfPages(siteEntity, slice.nextPageable());
            slice.getContent().parallelStream().unordered()
                    .forEach(pageEntity -> {
                        Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                        collectIndexLemma(lemmaRankFromPage, pageEntity, siteEntity);
                        lemmaRankFromPage.clear();
                        addIndexEntityList();
                    });
            writeIndexRankToDb();
        }
    }

    private synchronized void addIndexEntityList() {
        indexEntityList.addAll(sliceLemmaRank);
        sliceLemmaRank.clear();
    }

    private synchronized void writeIndexRankToDb() {
        List<IndexEntity> indexEntityListToWrite = new ArrayList<>(indexEntityList);
        indexEntityList.clear();
        log.info("write to DB : " + indexEntityListToWrite.size());
        indexRepositoryService.addIndexEntityList(indexEntityListToWrite);
        indexEntityListToWrite.clear();
    }

    private synchronized void writeIndexRank() {
        List<IndexEntity> indexEntityListToWrite = new ArrayList<>(indexEntityList);
        indexEntityList.clear();
        indexServiceAsync.writeIndexRank(indexEntityListToWrite);
    }

    private void collectIndexLemma(@NotNull Map<String, Float> partIndexLemma,
                                   PageEntity pageEntity,
                                   SiteEntity siteEntity) {
        List<LemmaEntity> lemmaEntityList = lemmaRepositoryService.getAllLemmaEntityBySiteEntity(siteEntity);
        partIndexLemma.forEach((lemma, rank) -> {
            LemmaEntity lemmaEntity = lemmaEntityList.parallelStream()
                    .unordered()
                    .filter(lem -> lem.getLemma().equals(lemma))
                    .findFirst()
                    .orElse(null);
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setLemmaRank(rank);
            indexEntity.setPage(pageEntity);
            indexEntity.setLemma(lemmaEntity);
            sliceLemmaRank.add(indexEntity);
        });
    }
}
