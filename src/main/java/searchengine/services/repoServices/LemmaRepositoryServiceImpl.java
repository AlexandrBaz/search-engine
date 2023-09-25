package searchengine.services.repoServices;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LemmaRepositoryServiceImpl implements LemmaRepositoryService {
    private LemmaRepository lemmaRepository;

    @Override
    public LemmaEntity getLemmaEntity(String lemma, SiteEntity siteEntity) {
        return lemmaRepository.findByLemmaAndSite(lemma, siteEntity).orElse(null);
    }

    @Override
    @Transactional
    public synchronized void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        lemmaRepository.deleteAllByIdInBatch(pageEntityListId);
    }

    @Override
    public List<LemmaEntity> getAllLemmaEntityBySiteEntity(SiteEntity siteEntity) {
        return lemmaRepository.findAllBySite(siteEntity);
    }

    @Override
    @Transactional
    public synchronized void addLemmaEntityList(@NotNull List<LemmaEntity> lemmaEntityList) {
        lemmaRepository.saveAllAndFlush(lemmaEntityList);
    }

    @Override
    public int getCountLemmaBySite(SiteEntity siteEntity) {
        return lemmaRepository.countBySite(siteEntity);
    }

    @Override
    @Transactional
    public void addPageLemmaEntityList(@NotNull List<LemmaEntity> lemmaEntityList) {
        lemmaEntityList.forEach(lemmaEntity -> {
            LemmaEntity presentedLemma = getLemmaEntity(lemmaEntity.getLemma(), lemmaEntity.getSite());
            if (presentedLemma != null){
                presentedLemma.setFrequency(presentedLemma.getFrequency() + 1);
                lemmaRepository.saveAndFlush(presentedLemma);
            } else {
                lemmaRepository.saveAndFlush(lemmaEntity);
            }
        });
    }

    @Autowired
    public void setLemmaRepository(LemmaRepository lemmaRepository){
        this.lemmaRepository = lemmaRepository;
    }
}
