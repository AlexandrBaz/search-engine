package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public interface IndexRepositoryService {

    List<IndexEntity> getListIndexEntity(PageEntity pageEntity);

    void deleteIndexEntity(List<IndexEntity> indexEntityList);

    void deleteByIdListPageEntity(List<Long> pageEntityListId);

    void addToIndexEntity(LemmaEntity lemmaEntity, Integer rank, PageEntity pageEntity);

    void addIndexEntityList(CopyOnWriteArrayList<IndexEntity> allRank);
}
