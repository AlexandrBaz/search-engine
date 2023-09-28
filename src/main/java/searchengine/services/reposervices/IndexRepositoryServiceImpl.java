package searchengine.services.reposervices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.repositories.IndexRepository;

import java.util.List;

@Service
public class IndexRepositoryServiceImpl implements IndexRepositoryService {

    private IndexRepository indexRepository;

    @Override
    public synchronized List<IndexEntity> getListIndexEntity(PageEntity pageEntity){
        return indexRepository.findAllByPage(pageEntity);
    }

    @Override
    public synchronized void addIndexEntityList(List<IndexEntity> allRank) {
        indexRepository.saveAllAndFlush(allRank);
    }

    @Override
    public synchronized IndexEntity getIndexEntity(LemmaEntity lemmaEntity, PageEntity pageEntity) {
        return indexRepository.findByLemmaAndPage(lemmaEntity,pageEntity).orElse(null);
    }

    @Override
    public synchronized void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        indexRepository.deleteAllByIdInBatch(pageEntityListId);
    }

    @Autowired
    public void setIndexRepository(IndexRepository indexRepository){
        this.indexRepository = indexRepository;
    }
}
