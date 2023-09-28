package searchengine.services.reposervices;

import org.springframework.stereotype.Service;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Service
public interface LemmaRepositoryService {
    LemmaEntity getLemmaEntity(String lemma, SiteEntity siteEntity);

    void deleteByIdListPageEntity(List<Long> pageEntityListId);

    List<LemmaEntity> getAllLemmaEntityBySiteEntity(SiteEntity siteEntity);

    void addLemmaEntityList(List<LemmaEntity> lemmaEntityList);

    int getCountLemmaBySite(SiteEntity siteEntity);

    void addPageLemmaEntityList(List<LemmaEntity> lemmaEntityList);
}
