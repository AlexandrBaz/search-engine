package searchengine.services.searchService;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.LemmaEntityStats;
import searchengine.dto.search.SearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.*;
import searchengine.services.IndexRepositoryService;
import searchengine.services.LemmaRepositoryService;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.utils.ServiceStore;
import searchengine.utils.lemma.LemmaFinder;

import java.io.IOException;
import java.util.*;

@Service
@Log4j2
public class SearchServiceImpl implements SearchService {

    private final static int PERCENT_ACCEPT = 80;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final SiteRepositoryService siteRepositoryService;
    private String query;

    @Autowired
    public SearchServiceImpl(@NotNull ServiceStore serviceStore) {
        this.indexRepositoryService = serviceStore.getIndexRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
    }

    @Override
    public SearchResponse getPages(String query, String site, Integer offset, Integer limit) {
        long start = System.currentTimeMillis();
        setQuery(query);
        SearchResponse searchResponse = new SearchResponse();
        viewLogInfo(query, site, offset, limit);
        Map<String, Integer> queryWords = getLemmaFinder().collectLemmas(query);
        if (site.equals("all")) {
            List<SiteEntity> siteEntityList = siteRepositoryService.getSiteByStatus(Status.INDEXED);
            List<SearchItem> searchItemList = new ArrayList<>();
            siteEntityList.forEach(siteEntity -> searchItemList.addAll(getPageRank(queryWords.keySet(), siteEntity)));
            List<SearchItem> searchItemSortedList = getRelevanceAndSort(searchItemList);
            searchResponse.setResult(true);
            searchResponse.setCount(searchItemSortedList.size());
            searchResponse.setData(searchItemSortedList);
        } else {
            SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.concat("/"));
            getPageRank(queryWords.keySet(), siteEntity);
//            getListByQueryBySite(queryWords, site);
        }
        log.info("Search completed for " + (System.currentTimeMillis() - start) + " ms");
        return searchResponse;
    }

    private @NotNull List<SearchItem> getPageRank(@NotNull Set<String> queryWords, SiteEntity siteEntity) {
        Set<LemmaEntityStats> lemmaEntityStatsSet = new TreeSet<>(Comparator.comparing(LemmaEntityStats::getPercent));
        queryWords.forEach(query -> {
            long pageCountBySite = pageRepositoryService.getCountPageBySite(siteEntity);
            LemmaEntity lemmaEntity = lemmaRepositoryService.getLemmaEntity(query, siteEntity);
            if (lemmaEntity != null) {
                float percent = ((float) lemmaEntity.getFrequency() / (float) pageCountBySite) * 100;
                if (percent < PERCENT_ACCEPT) {
                    lemmaEntityStatsSet.add(new LemmaEntityStats(lemmaEntity, percent));
                }
            }
        });
        List<PageEntity> pageEntityList = getListPageEntityByQuery(lemmaEntityStatsSet);
        return setPageRank(pageEntityList, lemmaEntityStatsSet);
    }

    private @NotNull List<PageEntity> getListPageEntityByQuery(@NotNull Set<LemmaEntityStats> lemmaEntityStatsSet) {
        List<PageEntity> finalPageEntityList = new ArrayList<>();
        for (LemmaEntityStats lemmaEntityStats : lemmaEntityStatsSet) {
            List<PageEntity> pageEntityList = pageEntityList(lemmaEntityStats.getLemmaEntity());
            if (finalPageEntityList.isEmpty()) {
                finalPageEntityList.addAll(pageEntityList);
            } else {
                finalPageEntityList.retainAll(pageEntityList);
            }
            if (finalPageEntityList.isEmpty()) {
                return finalPageEntityList;
            }
        }
        return finalPageEntityList;
    }

    private List<PageEntity> pageEntityList(@NotNull LemmaEntity lemmaEntity) {
        return lemmaEntity.getIndexLemmaEntities()
                .stream()
                .parallel()
                .unordered()
                .map(IndexEntity::getPage)
                .toList();
    }

    private @NotNull List<SearchItem> setPageRank(@NotNull List<PageEntity> pageEntityList, Set<LemmaEntityStats> lemmaEntityStatsSet) {
        SearchItemCreator searchItemCreator = new SearchItemCreator();
        List<SearchItem> searchItemList = new ArrayList<>();
        pageEntityList.forEach(pageEntity -> {
            SearchItem searchItem = new SearchItem();
            SearchItem finalSearchItem = searchItem;
            lemmaEntityStatsSet.forEach(lemmaEntityStats -> {
                float lemmaRank = indexRepositoryService.getIndexEntity(lemmaEntityStats.getLemmaEntity(), pageEntity).getLemmaRank();
                finalSearchItem.setAbsoluteRelevance(finalSearchItem.getAbsoluteRelevance() + lemmaRank);
            });
            searchItem = searchItemCreator.createSearchItem(pageEntity, searchItem, getQuery());
            searchItemList.add(searchItem);
        });
        return searchItemList;
    }

    private @NotNull List<SearchItem> getRelevanceAndSort(@NotNull List<SearchItem> searchItemList) {
        SearchItem maxAbsRelevance = Collections.max(searchItemList, Comparator.comparing(SearchItem::getAbsoluteRelevance));
        List<SearchItem> searchItemSortedList = new ArrayList<>(searchItemList.stream()
                .parallel()
                .unordered()
                .peek(searchItem -> searchItem.setRelevance(searchItem.getAbsoluteRelevance() / maxAbsRelevance.getAbsoluteRelevance()))
                .toList());
        searchItemSortedList.sort(Comparator.comparing(SearchItem::getAbsoluteRelevance).reversed());
        System.out.println("after sorting");
        System.out.println("-------------");
        searchItemSortedList.forEach(searchItem -> System.out.println(searchItem.getUri() + "\n"
                + "MaxRelevance: " + searchItem.getRelevance() + "\n"
                + "absRelevance: " + searchItem.getAbsoluteRelevance() + "\n"
                + searchItem.getTitle() + "\n"
                + searchItem.getSnippet() + "\n"
                + "-------------------------------------------"));
        return searchItemList;
    }


    private void viewLogInfo(String query, String site, Integer offset, Integer limit) {
        log.info(query + " " + site + " " + offset + " " + limit);
        Set<String> queryWords = getLemmaFinder().getLemmaSet(query);
        System.out.println("----------------------------");
        queryWords.forEach(System.out::println);
        System.out.println("----------------------------");

    }

    private void setQuery(String query) {
        this.query = query;
    }

    private @NotNull String getQuery() {
        return this.query.replaceAll("[.,?!]", "").toLowerCase();
    }

    private LemmaFinder getLemmaFinder() {
        LemmaFinder lemmaFinder;
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lemmaFinder;
    }
}