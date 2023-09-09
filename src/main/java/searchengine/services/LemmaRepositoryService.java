package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Map;

@Service
public interface LemmaRepositoryService {
    LemmaEntity getLemmaEntity(String lemma, SiteEntity siteEntity);

    void deleteLemmaOnPage(List<IndexEntity> indexEntityList);

    void deleteByIdListPageEntity(List<Long> pageEntityListId);

    void addNewLemma(Map<String, Integer> lemmaMap, PageEntity pageEntity);

    List<LemmaEntity> getAllLemmaEntityBySiteEntity(SiteEntity siteEntity);

    void addLemmaEntityList(List<LemmaEntity> lemmaEntityList);

    int getCountLemmaBySite(SiteEntity siteEntity);

    void addPageLemmaEntityList(List<LemmaEntity> lemmaEntityList);

    long getCountLemmaByLemmaAndSite(String query, SiteEntity siteEntity);
}
