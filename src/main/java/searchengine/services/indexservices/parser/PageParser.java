package searchengine.services.indexservices.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.AppConfig;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.reposervices.IndexRepositoryService;
import searchengine.services.reposervices.LemmaRepositoryService;
import searchengine.services.reposervices.PageRepositoryService;
import searchengine.services.reposervices.SiteRepositoryService;
import searchengine.services.indexservices.lemma.LemmaFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
@Getter
@Setter
@Log4j2
public class PageParser {
    private String url;
    private String domain;
    private String path;
    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final IndexRepositoryService indexRepositoryService;
    private final LemmaFinder lemmaFinder;
    private final AppConfig appConfig;

    @Autowired
    public PageParser(SiteRepositoryService siteRepositoryService, PageRepositoryService pageRepositoryService,
                      LemmaRepositoryService lemmaRepositoryService, IndexRepositoryService indexRepositoryService,
                      LemmaFinder lemmaFinder, AppConfig appConfig) {
        this.siteRepositoryService = siteRepositoryService;
        this.pageRepositoryService = pageRepositoryService;
        this.lemmaRepositoryService = lemmaRepositoryService;
        this.indexRepositoryService = indexRepositoryService;
        this.lemmaFinder = lemmaFinder;
        this.appConfig = appConfig;
    }

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
            log.info("Time elapsed {} ms. Collect lemma for url {}", (System.currentTimeMillis() - start), newPageEntity.getPath());
        }
    }

    private @NotNull PageEntity pageParse(String url, SiteEntity siteEntity) {
        PageEntity pageEntity = new PageEntity();
        Connection.Response response;
        try {
            response = Jsoup.connect(url)
                    .userAgent(appConfig.getUserAgent())
                    .timeout(appConfig.getTimeOut())
                    .ignoreHttpErrors(appConfig.isIgnoreHttpErrors())
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
        return !pageEntity.getCode().toString().matches(appConfig.getCode4xx5xx());
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
        Map<String, Integer> lemmaOnPage = lemmaFinder.collectLemmas(pageEntity.getContent());
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
         lemmaFinder.getAllIndexRankOfPage(pageEntity).forEach((lemma, rank) -> {
             LemmaEntity lemmaEntity = lemmaRepositoryService.getLemmaEntity(lemma, pageEntity.getSite());
             IndexEntity indexEntity = new IndexEntity();
             indexEntity.setPage(pageEntity);
             indexEntity.setLemma(lemmaEntity);
             indexEntity.setLemmaRank(rank);
             indexEntityList.add(indexEntity);
         });
        return indexEntityList;
    }

//    private LemmaFinder getLemmaFinder() {
//        LemmaFinder lemmaFinder;
//        try {
//            lemmaFinder = LemmaFinder.getInstance();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return lemmaFinder;
//    }
}