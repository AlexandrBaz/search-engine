package searchengine.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class LemmaRepositoryServiceImpl implements LemmaRepositoryService{
    private LemmaRepository lemmaRepository;
    private IndexRepositoryService indexRepositoryService;

    @Override
    public LemmaEntity getLemmaEntity(String lemma, SiteEntity siteEntity) {
        return lemmaRepository.findByLemmaAndSite(lemma, siteEntity).orElse(null);
    }

    @Override
    @Transactional
    public void deleteLemmaOnPage(@NotNull List<IndexEntity> indexEntityList) {
        indexEntityList.forEach(indexEntity -> {
            LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSite(indexEntity.getLemma().getLemma(), indexEntity.getLemma().getSite()).orElse(null);
            if (Objects.requireNonNull(lemmaEntity).getFrequency() == 1) {
                lemmaRepository.deleteById(indexEntity.getLemma().getId());
            } else {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() - 1);
                lemmaRepository.saveAndFlush(lemmaEntity);
            }
        });
    }

    @Override
    @Transactional
    public synchronized void addNewLemma(@NotNull Map<String, Integer> lemmaMap, PageEntity pageEntity) {
        lemmaMap.forEach((lemma, rank) -> {
            LemmaEntity lemmaEntity = getLemmaEntity(lemma, pageEntity.getSite());
            if (lemmaEntity != null) {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaRepository.saveAndFlush(lemmaEntity);
                indexRepositoryService.addToIndexEntity(lemmaEntity, rank, pageEntity);
            } else {
                LemmaEntity newLemmaEntity = new LemmaEntity();
                newLemmaEntity.setLemma(lemma);
                newLemmaEntity.setFrequency(1);
                newLemmaEntity.setSite(pageEntity.getSite());
                lemmaRepository.saveAndFlush(newLemmaEntity);
                indexRepositoryService.addToIndexEntity(newLemmaEntity, rank, pageEntity);
            }

        });
    }

    @Override
    @Transactional
    public synchronized void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        lemmaRepository.deleteAllById(pageEntityListId);

    }

    @Override
    public List<LemmaEntity> getAllLemmaEntityBySiteEntity(SiteEntity siteEntity) {
        return lemmaRepository.findAllBySite(siteEntity);
    }

    @Override
    @Transactional
    public synchronized void addLemmaEntityList(@NotNull Map<String, LemmaEntity> mapLemmaEntity) {
        List<LemmaEntity> lemmaEntities = mapLemmaEntity.values().stream().toList();
        lemmaRepository.saveAll(lemmaEntities);
    }

    @Override
    public int getCountLemmaBySite(SiteEntity siteEntity) {
        return lemmaRepository.countBySite(siteEntity);
    }

// Worked Methods
//    public void addToIndexEntity(String lemma, Integer rank, PageEntity pageEntity) {// this IndexEntity
//        SiteEntity siteEntity = pageEntity.getSite();
//        LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSite(lemma, siteEntity).orElse(null);
//        indexEntityDAO.addIndex(lemmaEntity, rank, pageEntity);
//    }

    @Autowired
    public void setLemmaRepository(LemmaRepository lemmaRepository){
        this.lemmaRepository = lemmaRepository;
    }

    @Autowired
    public void setIndexRepositoryService(IndexRepositoryService indexRepositoryService){
        this.indexRepositoryService = indexRepositoryService;
    }
}
