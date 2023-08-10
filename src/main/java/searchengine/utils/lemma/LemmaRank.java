package searchengine.utils.lemma;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.IndexRepositoryService;
import searchengine.services.LemmaRepositoryService;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.utils.ServiceStore;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LemmaRank {
    private final static Integer BATCH_SIZE = 10;
    private final SiteRepositoryService siteRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final static CopyOnWriteArrayList<IndexEntity> allRank = new CopyOnWriteArrayList<>();

    public LemmaRank(@NotNull ServiceStore serviceStore) {
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.indexRepositoryService = serviceStore.getIndexRepositoryService();
    }

    public void lemmaRankBySite(String domain) {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
        List<LemmaEntity> lemmaEntityList = lemmaRepositoryService.getAllLemmaEntityBySiteEntity(siteEntity);
        Slice<PageEntity> slice = pageRepositoryService.getSliceOfPages(siteEntity, PageRequest.of(0, BATCH_SIZE));
        slice.getContent().stream().parallel()
                .forEach(pageEntity -> collectIndexLemma(getAllIndexRankOfPage(pageEntity), pageEntity, lemmaEntityList));
        while (slice.hasNext()) {
            slice = pageRepositoryService.getSliceOfPages(siteEntity, slice.nextPageable());
            slice.getContent().stream().parallel()
                    .forEach(pageEntity -> collectIndexLemma(getAllIndexRankOfPage(pageEntity), pageEntity, lemmaEntityList));
        }
        indexRepositoryService.addIndexEntityList(allRank);
    }

    private ConcurrentHashMap<String, Float> getAllIndexRankOfPage(@NotNull PageEntity pageEntity) {
        ConcurrentHashMap<String, Float> pageIndexMap = new ConcurrentHashMap<>();
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            lemmaFinder.partitionDocument(pageEntity.getContent()).forEach((partName, text) -> {
                ConcurrentHashMap<String, Float> lemmaFromPagePart = switch (partName) {
                    case ("title") ->
                            getLemmaFromPagePart(lemmaFinder, text, IndexLemmaRank.TITLE.getMultiplier());
                    case ("description") ->
                            getLemmaFromPagePart(lemmaFinder, text, IndexLemmaRank.DESCRIPTION.getMultiplier());
                    case ("h1Elements") ->
                            getLemmaFromPagePart(lemmaFinder, text, IndexLemmaRank.H1ELEMENTS.getMultiplier());
                    case ("h2Elements") ->
                            getLemmaFromPagePart(lemmaFinder, text, IndexLemmaRank.H2ELEMENTS.getMultiplier());
                    case ("footer") ->
                            getLemmaFromPagePart(lemmaFinder, text, IndexLemmaRank.FOOTER.getMultiplier());
                    default -> getLemmaFromPagePart(lemmaFinder, text, IndexLemmaRank.BODY.getMultiplier());
                };
                lemmaFromPagePart.forEach((lemma, rank) -> {
                    if (pageIndexMap.containsKey(lemma)) {
                        pageIndexMap.computeIfPresent(lemma, (key, value) -> value + rank);
                    } else {
                        pageIndexMap.put(lemma, rank);
                    }
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pageIndexMap;
    }

    private @NotNull ConcurrentHashMap<String, Float> getLemmaFromPagePart(@NotNull LemmaFinder lemmaFinder, String partOfPage, Float lemmaMultiplier) {
        Map<String, Integer> temporaryMap = lemmaFinder.collectLemmas(partOfPage);
        ConcurrentHashMap<String, Float> finalMap = new ConcurrentHashMap<>();
        temporaryMap.forEach((key, value) -> finalMap.put(key, value * lemmaMultiplier));
        return finalMap;
    }

    private void collectIndexLemma(@NotNull ConcurrentHashMap<String, Float> partIndexLemma,
                                   PageEntity pageEntity,
                                   List<LemmaEntity> lemmaEntityList) {
        partIndexLemma.forEach((lemma, rank) -> {
            LemmaEntity lemmaEntity = lemmaEntityList.stream().parallel().filter(lem -> lem.getLemma().equals(lemma)).findFirst().orElse(null);
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setLemmaRank(rank);
            indexEntity.setPage(pageEntity);
            indexEntity.setLemma(lemmaEntity);
            allRank.add(indexEntity);
        });

    }
}
