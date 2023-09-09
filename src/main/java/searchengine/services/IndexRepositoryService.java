package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public interface IndexRepositoryService {

    List<IndexEntity> getListIndexEntity(PageEntity pageEntity);

    void deleteIndexEntity(List<IndexEntity> indexEntityList);

    void deleteByIdListPageEntity(List<Long> pageEntityListId);

    void addToIndexEntity(LemmaEntity lemmaEntity, Integer rank, PageEntity pageEntity);

    void addIndexEntityList(CopyOnWriteArrayList<IndexEntity> allRank);

    long getCountIndexByLemmaAndSite(LemmaEntity lemmaEntity, SiteEntity siteEntity);

    List<IndexEntity> getListIndexEntityByLemma(LemmaEntity lemmaEntity);

    IndexEntity getIndexEntity(LemmaEntity lemmaEntity, PageEntity pageEntity);
}
