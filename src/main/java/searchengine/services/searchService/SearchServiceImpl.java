package searchengine.services.searchService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import searchengine.dto.search.*;
import searchengine.model.*;
import searchengine.services.IndexRepositoryService;
import searchengine.services.LemmaRepositoryService;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.utils.ServiceStore;
import searchengine.utils.lemma.LemmaFinder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
@Setter
@Getter
@Log4j2
public class SearchServiceImpl implements SearchService {

    private final int PERCENT_ACCEPT = 80;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final SiteRepositoryService siteRepositoryService;
    private final LRUCache<String, List<SearchItemCached>> cache = new LRUCache<>(3);
    private String query;
    private int offset;
    private int limit;

    @Autowired
    public SearchServiceImpl(@NotNull ServiceStore serviceStore) {
        this.indexRepositoryService = serviceStore.getIndexRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
    }

    @Override
    public SearchResponse getPages(String query, String site, Integer offset, Integer limit) {
//        viewLogInfo(query, site, offset, limit);
        long start = System.currentTimeMillis();
        String key = query + "|" + site;
        setQuery(query);
        setOffset(offset);
        setLimit(limit);

        List<String> queryByWords = getLemmaFinder().collectLemmas(query).keySet().stream().toList();
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
        List<SearchItemCached> searchItemCachedList = getSearchItemCachedListByALlSite(queryByWords);
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
        QueryCallableBySite queryCallableBySite = new QueryCallableBySite(queryByWords, siteEntity, this);
        List<SearchItemCached> searchItemCachedList = queryCallableBySite.call();
        if (searchItemCachedList.isEmpty()) {
            return getEmptyResponse(start);
        } else {
            List<SearchItemCached> sortedSearchItemCachedList = getRelevanceAndSort(searchItemCachedList);
            cache.put(key, sortedSearchItemCachedList);
            return getSearchItemList(getOffset(), getLimit(), sortedSearchItemCachedList, start);
        }
    }

    private @NotNull SearchResponse getEmptyResponse(long start) {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setData(Collections.emptyList());
        searchResponse.setCount(0L);
        searchResponse.setResult(true);
        log.info("searchResponse completed for " + (System.currentTimeMillis() - start) + " ms");
        return searchResponse;
    }

    private @NotNull List<SearchItemCached> getSearchItemCachedListByALlSite(List<String> queryByWords) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<SiteEntity> siteEntityList = siteRepositoryService.getSiteByStatus(Status.INDEXED);
        List<Future<List<SearchItemCached>>> futureList = new ArrayList<>();
        siteEntityList.forEach(siteEntity -> {
            QueryCallableBySite queryCallable = new QueryCallableBySite(queryByWords, siteEntity, this);
            Future<List<SearchItemCached>> future = executor.submit(queryCallable);
            futureList.add(future);
        });
        executor.shutdown();
        List<SearchItemCached> searchItemCachedList = new ArrayList<>();
        futureList.forEach(future -> {
            try {
                searchItemCachedList.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        return searchItemCachedList;
    }

    private @NotNull List<SearchItemCached> getRelevanceAndSort(@NotNull List<SearchItemCached> searchItemCachedList) {
        float maxAbsRelevance = Collections.max(searchItemCachedList, Comparator.comparing(SearchItemCached::getAbsoluteRelevance)).getAbsoluteRelevance();
        return new ArrayList<>(searchItemCachedList.parallelStream()
                .peek(searchItemCached -> searchItemCached.setRelevance(searchItemCached.getAbsoluteRelevance() / maxAbsRelevance))
                .sorted(Comparator.comparing(SearchItemCached::getAbsoluteRelevance).reversed())
                .toList());
    }

    private @NotNull SearchResponse getSearchItemList(int offset, int limit, List<SearchItemCached> searchItemCachedList, long start) {
        SearchItemCreator searchItemCreator = new SearchItemCreator(pageRepositoryService);
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


    private void viewLogInfo(String query, String site, Integer offset, Integer limit) {
        log.info(query + " " + site + " " + offset + " " + limit);
        Set<String> queryWords = getLemmaFinder().getLemmaSet(query);
        System.out.println("----------------------------");
        queryWords.forEach(System.out::println);
        System.out.println("----------------------------");

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