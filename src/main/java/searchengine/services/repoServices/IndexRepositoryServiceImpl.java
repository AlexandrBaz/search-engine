package searchengine.services.repoServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.repositories.IndexRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class IndexRepositoryServiceImpl implements IndexRepositoryService {

    private IndexRepository indexRepository;

    @Override
    public List<IndexEntity> getListIndexEntity(PageEntity pageEntity){
        return indexRepository.findAllByPage(pageEntity);
    }

    @Override
    @Transactional
    public synchronized void addIndexEntityList(List<IndexEntity> allRank) {
        indexRepository.saveAllAndFlush(allRank);
    }

    @Override
    public synchronized IndexEntity getIndexEntity(LemmaEntity lemmaEntity, PageEntity pageEntity) {
        return indexRepository.findByLemmaAndPage(lemmaEntity,pageEntity).orElse(null);
    }

    @Override
    @Transactional
    public synchronized void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        indexRepository.deleteAllByIdInBatch(pageEntityListId);
    }

    @Autowired
    public void setIndexRepository(IndexRepository indexRepository){
        this.indexRepository = indexRepository;
    }
}
