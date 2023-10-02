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
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexservices.lemma.LemmaFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
@Getter
@Setter
@Log4j2
public class PageParser {
    private String url;
    private String domain;
    private String path;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaFinder lemmaFinder;
    private final AppConfig appConfig;

    @Autowired
    public PageParser(SiteRepository siteRepository, PageRepository pageRepository,
                      LemmaRepository lemmaRepository, IndexRepository indexRepository,
                      LemmaFinder lemmaFinder, AppConfig appConfig) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaFinder = lemmaFinder;
        this.appConfig = appConfig;
    }

    public void addOrUpdatePage() {
        SiteEntity siteEntity = siteRepository.findByUrl(domain).orElse(null);
        PageEntity newPageEntity = pageParse(url, siteEntity);
        long start = System.currentTimeMillis();
        if (urlHasCorrectAnswer(newPageEntity)) {
            if (pageIsAlreadyParsed()) {
                deleteOldDataForThisPageEntity(newPageEntity);
            } else {
               pageRepository.saveAndFlush(newPageEntity);
            }
            addPageLemmaEntityList(getLemmaEntityList(newPageEntity));
            indexRepository.saveAllAndFlush(getIndexEntityList(newPageEntity));
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
        SiteEntity siteEntity = siteRepository.findByUrl(domain).orElse(null);
        return pageRepository.findByPathAndSite(path, siteEntity).isPresent();
    }

    private boolean urlHasCorrectAnswer(@NotNull PageEntity pageEntity) {
        return !pageEntity.getCode().toString().matches(appConfig.getCode4xx5xx());
    }

    private void deleteOldDataForThisPageEntity(@NotNull PageEntity pageEntity) {
        PageEntity oldPageEntity = pageRepository.findByPathAndSite(pageEntity.getPath(), pageEntity.getSite()).orElse(null);
        List<IndexEntity> indexEntityList = indexRepository.findAllByPage(oldPageEntity);
        if(!indexEntityList.isEmpty()) {
            List<Long> idIndexEntityList = indexEntityList.stream().map(IndexEntity::getId).toList();
            List<Long> idLemmaEntityList = indexEntityList.stream().map(IndexEntity -> IndexEntity.getLemma().getId()).toList();
            indexRepository.deleteAllByIdInBatch(idIndexEntityList);
            lemmaRepository.deleteAllByIdInBatch(idLemmaEntityList);
        }
        pageRepository.delete(Objects.requireNonNull(oldPageEntity));
        pageRepository.saveAndFlush(pageEntity);
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
             LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSite(lemma, pageEntity.getSite()).orElse(null);
             IndexEntity indexEntity = new IndexEntity();
             indexEntity.setPage(pageEntity);
             indexEntity.setLemma(lemmaEntity);
             indexEntity.setLemmaRank(rank);
             indexEntityList.add(indexEntity);
         });
        return indexEntityList;
    }

    private void addPageLemmaEntityList(@NotNull List<LemmaEntity> lemmaEntityList) {
        lemmaEntityList.forEach(lemmaEntity -> {
            LemmaEntity presentedLemma = lemmaRepository.findByLemmaAndSite(lemmaEntity.getLemma(), lemmaEntity.getSite()).orElse(null);
            if (presentedLemma != null){
                presentedLemma.setFrequency(presentedLemma.getFrequency() + 1);
                lemmaRepository.saveAndFlush(presentedLemma);
            } else {
                lemmaRepository.saveAndFlush(lemmaEntity);
            }
        });
    }
}