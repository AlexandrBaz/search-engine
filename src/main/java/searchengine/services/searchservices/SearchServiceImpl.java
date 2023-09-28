package searchengine.services.searchservices;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import searchengine.dto.search.*;
import searchengine.model.*;
import searchengine.services.reposervices.IndexRepositoryService;
import searchengine.services.reposervices.LemmaRepositoryService;
import searchengine.services.reposervices.PageRepositoryService;
import searchengine.services.reposervices.SiteRepositoryService;
import searchengine.services.indexservices.lemma.LemmaFinder;

import java.util.*;

@Service
@Setter
@Getter
@Log4j2
public class SearchServiceImpl implements SearchService {
    @Value(value = "${search.percentAccept}")
    private int PERCENT_ACCEPT;
    private final LRUCache<String, List<SearchItemCached>> cache;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final SiteRepositoryService siteRepositoryService;
    private final SearchItemCreator searchItemCreator;
    private final LemmaFinder lemmaFinder;
    private String query;
    private int offset;
    private int limit;

    @Autowired
    public SearchServiceImpl(SearchItemCreator searchItemCreator, @Value(value = "${search.lruSize}") int cacheSize,
                             LemmaRepositoryService lemmaRepositoryService, IndexRepositoryService indexRepositoryService,
                             PageRepositoryService pageRepositoryService, SiteRepositoryService siteRepositoryService, LemmaFinder lemmaFinder) {
        this.searchItemCreator = searchItemCreator;
        this.cache = new LRUCache<>(cacheSize);
        this.lemmaRepositoryService = lemmaRepositoryService;
        this.indexRepositoryService = indexRepositoryService;
        this.pageRepositoryService = pageRepositoryService;
        this.siteRepositoryService = siteRepositoryService;
        this.lemmaFinder = lemmaFinder;
    }

    @Override
    public SearchResponse getPages(@NotNull String query, String site, Integer offset, Integer limit) {
        String clearQuery = query.trim();
        long start = System.currentTimeMillis();
        String key = clearQuery + "|" + site;
        setQuery(clearQuery);
        setOffset(offset);
        setLimit(limit);

        List<String> queryByWords = lemmaFinder.collectLemmas(clearQuery).keySet().stream().toList();
        if (cache.keyIsPresent(key)) {
            return getSearchItemList(offset, limit, cache.get(key), start);
        }
        if (site.equals("all")) {
            return getResultForALlSite(key, queryByWords, start);
        } else {
            return getResultByOneSite(key, site, queryByWords, start);
        }
    }

    private SearchResponse getResultForALlSite(String key, List<String> queryByWords, long start) {
        List<SearchItemCached> searchItemCachedList = getResultByAllSite(queryByWords);
        if (searchItemCachedList.isEmpty()) {
            return getEmptyResponse(start);
        } else {
            List<SearchItemCached> sortedSearchItemCachedList = getRelevanceAndSort(searchItemCachedList);
            cache.put(key, sortedSearchItemCachedList);
            return getSearchItemList(getOffset(), getLimit(), sortedSearchItemCachedList, start);
        }
    }

    private SearchResponse getResultByOneSite(String key, @NotNull String site, List<String> queryByWords, long start) {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site);
        ItemCachedCreator itemCachedCreator = new ItemCachedCreator(queryByWords, siteEntity, this);
        List<SearchItemCached> searchItemCachedList = itemCachedCreator.getSearchItemCachedList();
        if (searchItemCachedList.isEmpty()) {
            return getEmptyResponse(start);
        } else {
            List<SearchItemCached> sortedSearchItemCachedList = getRelevanceAndSort(searchItemCachedList);
            cache.put(key, sortedSearchItemCachedList);
            return getSearchItemList(getOffset(), getLimit(), sortedSearchItemCachedList, start);
        }
    }

    private @NotNull List<SearchItemCached> getResultByAllSite(List<String> queryByWords) {
        List<SiteEntity> siteEntityList = siteRepositoryService.getSiteByStatus(Status.INDEXED);
        List<SearchItemCached> searchItemCachedList = new ArrayList<>();
        siteEntityList.forEach(siteEntity -> {
            ItemCachedCreator itemCachedCreator = new ItemCachedCreator(queryByWords, siteEntity, this);
            searchItemCachedList.addAll(itemCachedCreator.getSearchItemCachedList());
        });
        return searchItemCachedList;
    }


    private @NotNull SearchResponse getEmptyResponse(long start) {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setData(Collections.emptyList());
        searchResponse.setCount(0L);
        searchResponse.setResult(true);
        log.info("searchResponse completed for " + (System.currentTimeMillis() - start) + " ms");
        return searchResponse;
    }

    private @NotNull List<SearchItemCached> getRelevanceAndSort(@NotNull List<SearchItemCached> searchItemCachedList) {
        float maxAbsRelevance = Collections.max(searchItemCachedList, Comparator.comparing(SearchItemCached::getAbsoluteRelevance)).getAbsoluteRelevance();
        return new ArrayList<>(searchItemCachedList.parallelStream()
                .peek(searchItemCached -> searchItemCached.setRelevance(searchItemCached.getAbsoluteRelevance() / maxAbsRelevance))
                .sorted(Comparator.comparing(SearchItemCached::getAbsoluteRelevance).reversed())
                .toList());
    }

    private @NotNull SearchResponse getSearchItemList(int offset, int limit, List<SearchItemCached> searchItemCachedList, long start) {
        Page<SearchItemCached> searchItemCachedPage = createPageSearchItems(offset, limit, searchItemCachedList);
        List<SearchItem> searchItemList = searchItemCreator.createSearchItem(searchItemCachedPage, getQuery());
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(true);
        searchResponse.setData(searchItemList);
        searchResponse.setCount(searchItemCachedPage.getTotalElements());
        log.info("searchResponse completed for " + (System.currentTimeMillis() - start) + " ms");
        return searchResponse;
    }

    public Page<SearchItemCached> createPageSearchItems(int offset, int limit, @NotNull List<SearchItemCached> searchItemCachedList) {
        List<SearchItemCached> pageSearchItems;
        int page = offset / limit;
        if (searchItemCachedList.size() < offset) {
            pageSearchItems = Collections.emptyList();
        } else {
            int toIndex = Math.min(offset + limit, searchItemCachedList.size());
            pageSearchItems = searchItemCachedList.subList(offset, toIndex);
        }
        return new PageImpl<>(pageSearchItems, PageRequest.of(page, limit), searchItemCachedList.size());
    }

    private @NotNull String getQuery() {
        return this.query.replaceAll("[.,?!]", "").toLowerCase();
    }
}