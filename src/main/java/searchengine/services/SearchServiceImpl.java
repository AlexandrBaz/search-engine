package searchengine.services;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.index.Response;
import searchengine.dto.search.LemmaEntityStats;
import searchengine.dto.search.PageRank;
import searchengine.model.*;
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


    public SearchServiceImpl(ServiceStore serviceStore) {
        this.indexRepositoryService = serviceStore.getIndexRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
    }

    @Override
    public Response getPages(String query, String site, Integer offset, Integer limit) {
        viewLogInfo(query, site, offset, limit);
        Map<String, Integer> queryWords = getLemmaFinder().collectLemmas(query);
        if (site.equals("all")) {
            List<SiteEntity> siteEntityList = siteRepositoryService.getSiteByStatus(Status.INDEXED);
            Map<String, PageRank> pageRankMap = new HashMap<>();
            siteEntityList.forEach(siteEntity -> {
                pageRankMap.putAll(getPageRank(queryWords.keySet(), siteEntity));
            });
            setRelevanceAndSort(pageRankMap);
        } else {
            SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.concat("/"));
            getPageRank(queryWords.keySet(), siteEntity);
//            getListByQueryBySite(queryWords, site);
        }
        return null;
    }

    private @NotNull Map<String, PageRank> getPageRank(@NotNull Set<String> queryWords, SiteEntity siteEntity) {
        Set<LemmaEntityStats> lemmaEntityStatsSet = new TreeSet<>(Comparator.comparing(LemmaEntityStats::getPercent));
        queryWords.forEach(query -> {
            long pageCountBySite = pageRepositoryService.getCountPageBySite(siteEntity);
            LemmaEntity lemmaEntity = lemmaRepositoryService.getLemmaEntity(query, siteEntity);
            if (lemmaEntity != null) {
                float percent = ((float) lemmaEntity.getFrequency() / (float) pageCountBySite) * 100;
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                System.out.println(lemmaEntity.getLemma() + " " + percent);
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                if (percent < PERCENT_ACCEPT) {
                    lemmaEntityStatsSet.add(new LemmaEntityStats(lemmaEntity, percent));
                }
            }
        });
        System.out.println("+++++++++++++++++++++++++++");
        lemmaEntityStatsSet.forEach(lemmaEntityStats -> System.out.println(lemmaEntityStats.getLemmaEntity().getLemma() + " " + lemmaEntityStats.getPercent()));
        System.out.println("+++++++++++++++++++++++++++");
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

    private @NotNull Map<String, PageRank> setPageRank(@NotNull List<PageEntity> pageEntityList, Set<LemmaEntityStats> lemmaEntityStatsSet) {
        Map<String, PageRank> pageRankMap = new TreeMap<>();
        pageEntityList.forEach(pageEntity -> {
            PageRank pageRank = new PageRank();
            lemmaEntityStatsSet.forEach(lemmaEntityStats -> {
                float lemmaRank = indexRepositoryService.getIndexEntity(lemmaEntityStats.getLemmaEntity(), pageEntity).getLemmaRank();
                pageRank.setDomain(pageEntity.getSite().getUrl());
                pageRank.setSiteName(pageEntity.getSite().getName());
                pageRank.setUri(pageEntity.getPath());
                pageRank.setAbsRelevance(pageRank.getAbsRelevance() + lemmaRank);
            });
            pageRankMap.put(pageRank.getDomain().concat(pageRank.getUri().replaceFirst("/", "")), pageRank);
        });
        return pageRankMap;
    }

    private void setRelevanceAndSort(@NotNull Map<String, PageRank> finalPageRankMap) {
        List<PageRank> pageRankList = new ArrayList<>(finalPageRankMap.values());
        PageRank maxAbsRelevance = Collections.max(pageRankList, Comparator.comparing(PageRank::getAbsRelevance));
        List<PageRank> relevancePageRank = new ArrayList<>(pageRankList.stream()
                .parallel()
                .unordered()
                .peek(pageRank -> pageRank.setRelevance(pageRank.getAbsRelevance() / maxAbsRelevance.getAbsRelevance()))
                .toList());
        relevancePageRank.sort(Comparator.comparing(PageRank::getAbsRelevance).reversed());
        System.out.println("after sorting");
        System.out.println("-------------");
        relevancePageRank.forEach(pageRank -> System.out.println(pageRank.getUri() + " MaxRelevance: " + pageRank.getRelevance() + " absRelevance: " + pageRank.getAbsRelevance()));
    }

    private void setTitleAndSnippet(@NotNull PageEntity pageEntity, @NotNull PageRank pageRank, @NotNull Set<LemmaEntityStats> lemmaEntityStats) {
        Map<String, String> partitionDocument = getLemmaFinder().partitionDocument(pageEntity.getContent());
        Document document = Jsoup.parse(pageEntity.getContent());
        pageRank.setTitle(document.getElementsByTag("title").text());
        lemmaEntityStats.forEach(lemma -> {
            String query = lemma.getLemmaEntity().getLemma();
        });

    }

    private void viewLogInfo(String query, String site, Integer offset, Integer limit) {
        log.info(query + " " + site + " " + offset + " " + limit);
        Set<String> queryWords = getLemmaFinder().getLemmaSet(query);
        System.out.println("----------------------------");
        queryWords.forEach(System.out::println);
        System.out.println("----------------------------");

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
