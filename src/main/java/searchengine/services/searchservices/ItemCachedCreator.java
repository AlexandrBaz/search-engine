package searchengine.services.searchservices;

import org.jetbrains.annotations.NotNull;
import searchengine.dto.search.LemmaEntityRank;
import searchengine.dto.search.SearchItemCached;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemCachedCreator {
    private final List<String> queryByWords;
    private final SiteEntity siteEntity;
    private final SearchServiceImpl searchService;


    public ItemCachedCreator(List<String> queryByWords, SiteEntity siteEntity, SearchServiceImpl searchService) {
        this.queryByWords = queryByWords;
        this.siteEntity = siteEntity;
        this.searchService = searchService;
    }

    public List<SearchItemCached> getSearchItemCachedList() {
        Set<LemmaEntityRank> lemmaEntityRanks = getSearchQueryRankByWord();
        List<PageEntity> totalPageEntityListByQuery = getListPageEntityByQuery(lemmaEntityRanks);
        return setAbsoluteRelevance(lemmaEntityRanks, totalPageEntityListByQuery);
    }

    private @NotNull Set<LemmaEntityRank> getSearchQueryRankByWord() {
        Set<LemmaEntityRank> lemmaEntityRanks = new TreeSet<>(Comparator.comparing(LemmaEntityRank::getPercent));
        queryByWords.forEach(wordOfQuery ->{
            long totalPageBySite = searchService.getPageRepositoryService().getCountPageBySite(siteEntity);
            LemmaEntity lemmaEntity = searchService.getLemmaRepositoryService().getLemmaEntity(wordOfQuery,siteEntity);
            if (lemmaEntity != null) {
                float percent = ((float) lemmaEntity.getFrequency() / (float) totalPageBySite) * 100;
                if (percent < searchService.getPERCENT_ACCEPT()) {
                    lemmaEntityRanks.add(new LemmaEntityRank(lemmaEntity, percent));
                }
            }
        });
        return lemmaEntityRanks;
    }

    private @NotNull List<PageEntity> getListPageEntityByQuery(@NotNull Set<LemmaEntityRank> lemmaEntityRanks) {
        List<PageEntity> finalPageEntityList = new CopyOnWriteArrayList<>();
        lemmaEntityRanks.forEach(lemmaEntityRank -> {
            List<PageEntity> pageEntityList = pageEntityList(lemmaEntityRank.getLemmaEntity());
            if (finalPageEntityList.isEmpty()) {
                finalPageEntityList.addAll(pageEntityList);
            } else {
                finalPageEntityList.retainAll(pageEntityList);
            }
        });
        return finalPageEntityList;
    }

    private List<PageEntity> pageEntityList(@NotNull LemmaEntity lemmaEntity) {
        return lemmaEntity.getIndexLemmaEntities()
                .parallelStream()
                .map(IndexEntity::getPage)
                .toList();
    }

    private @NotNull List<SearchItemCached> setAbsoluteRelevance(Set<LemmaEntityRank> lemmaEntityRanks, @NotNull List<PageEntity> totalPageEntityListByQuery) {
        List<SearchItemCached> searchItemCachedList = new ArrayList<>();
        totalPageEntityListByQuery.parallelStream().forEach(pageEntity -> {
            SearchItemCached searchItemCached = new SearchItemCached();
            lemmaEntityRanks.parallelStream().forEach(lemmaEntityRank -> {
                IndexEntity indexEntity = searchService.getIndexRepositoryService().getIndexEntity(lemmaEntityRank.getLemmaEntity(), pageEntity);
                if (indexEntity != null) {
                    float lemmaRank = indexEntity.getLemmaRank();
                    searchItemCached.setAbsoluteRelevance(searchItemCached.getAbsoluteRelevance() + lemmaRank);
                    searchItemCached.setPageId(pageEntity.getId());
                }
            });
            searchItemCachedList.add(searchItemCached);
        });
        return searchItemCachedList;
    }
}
