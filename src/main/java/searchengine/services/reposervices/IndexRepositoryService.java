package searchengine.services.reposervices;

import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Service
public interface IndexRepositoryService {

    List<IndexEntity> getListIndexEntity(PageEntity pageEntity);

    void deleteByIdListPageEntity(List<Long> pageEntityListId);

    void addIndexEntityList(List<IndexEntity> allRank);

    IndexEntity getIndexEntity(LemmaEntity lemmaEntity, PageEntity pageEntity);
}
