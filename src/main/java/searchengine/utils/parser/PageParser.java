package searchengine.utils.parser;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.IndexRepositoryService;
import searchengine.services.LemmaRepositoryService;
import searchengine.services.PageRepositoryService;
import searchengine.services.SiteRepositoryService;
import searchengine.utils.ServiceStore;
import searchengine.utils.lemma.LemmaFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class PageParser {
    private final static String code4xx5xx = "[45]\\d{2}";
    private final String url;
    private final String domain;
    private final String path;
    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;

    public PageParser(@NotNull String url, String domain, @NotNull ServiceStore serviceStore) {
        this.url = url;
        this.domain = domain;
        this.path = url.replaceAll(domain, "/");
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
        this.indexRepositoryService = serviceStore.getIndexRepositoryService();
    }

    //Rewrite Methods

    public void addOrUpdatePage() {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
        PageEntity newPageEntity = pageParse(url, siteEntity);
        long start = System.currentTimeMillis();
        if (urlHasCorrectAnswer(newPageEntity)) {
            if (pageIsAlreadyParsed()) {
                deleteOldDataForThisPageEntity(newPageEntity);
            } else {
                pageRepositoryService.savePageEntity(newPageEntity);
            }
            lemmaRepositoryService.addPageLemmaEntityList(getLemmaEntityList(newPageEntity));
            indexRepositoryService.addIndexEntityList(getIndexEntityList(newPageEntity));
            long end = System.currentTimeMillis();
            System.out.println("Time elapsed " + (end - start) + " ms. Collect lemma for url " + newPageEntity.getPath() + " where id is " + newPageEntity.getId());
        }
    }

    private @NotNull PageEntity pageParse(String url, SiteEntity siteEntity) {
        PageEntity pageEntity = new PageEntity();
        Connection.Response response;
        try {
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru-RU) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.11 Safari/534.16")
                    .timeout(10000)
                    .ignoreHttpErrors(true)
//                    .followRedirects(false) // Здесь надо подумать, нужно ли вообще
                    .execute();
            Document document = response.parse();
            pageEntity.setContent(document.outerHtml());
            pageEntity.setCode(response.statusCode());
            pageEntity.setSite(siteEntity);
            pageEntity.setPath(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pageEntity;
    }

    private boolean pageIsAlreadyParsed() {
        return pageRepositoryService.pageEntityIsPresent(path, domain);
    }

    private boolean urlHasCorrectAnswer(@NotNull PageEntity pageEntity) {
        return !pageEntity.getCode().toString().matches(code4xx5xx);
    }

    private void deleteOldDataForThisPageEntity(@NotNull PageEntity pageEntity) {
        PageEntity oldPageEntity = pageRepositoryService.getPageEntity(pageEntity.getPath(), pageEntity.getSite());
        List<IndexEntity> indexEntityList = indexRepositoryService.getListIndexEntity(oldPageEntity);
        if(!indexEntityList.isEmpty()) {
            List<Long> idIndexEntityList = indexEntityList.stream().map(IndexEntity::getId).toList();
            List<Long> idLemmaEntityList = indexEntityList.stream().map(IndexEntity -> IndexEntity.getLemma().getId()).toList();
            indexRepositoryService.deleteByIdListPageEntity(idIndexEntityList);
            lemmaRepositoryService.deleteByIdListPageEntity(idLemmaEntityList);
        }
        pageRepositoryService.deletePage(oldPageEntity);
        pageRepositoryService.savePageEntity(pageEntity);
    }

    private @NotNull List<LemmaEntity> getLemmaEntityList(@NotNull PageEntity pageEntity) {
        Map<String, Integer> lemmaOnPage = getLemmaFinder().collectLemmas(pageEntity.getContent());
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        lemmaOnPage.forEach((lemma, count) -> {
            LemmaEntity lemmaEntity = new LemmaEntity();
            lemmaEntity.setLemma(lemma);
            lemmaEntity.setFrequency(count);
            lemmaEntity.setSite(pageEntity.getSite());
            lemmaEntityList.add(lemmaEntity);
        });
        return lemmaEntityList;
    }

    private @NotNull CopyOnWriteArrayList<IndexEntity> getIndexEntityList(PageEntity pageEntity) {
        CopyOnWriteArrayList<IndexEntity> indexEntityList = new CopyOnWriteArrayList<>();
         getLemmaFinder().getAllIndexRankOfPage(pageEntity).forEach((lemma, rank) -> {
             LemmaEntity lemmaEntity = lemmaRepositoryService.getLemmaEntity(lemma, pageEntity.getSite());
             IndexEntity indexEntity = new IndexEntity();
             indexEntity.setPage(pageEntity);
             indexEntity.setLemma(lemmaEntity);
             indexEntity.setLemmaRank(rank);
             indexEntityList.add(indexEntity);
         });
        return indexEntityList;
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
