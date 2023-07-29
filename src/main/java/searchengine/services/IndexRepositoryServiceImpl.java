package searchengine.services;

import org.jetbrains.annotations.NotNull;
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
public class IndexRepositoryServiceImpl implements IndexRepositoryService{

    private IndexRepository indexRepository;

    @Override
    public List<IndexEntity> getListIndexEntity(PageEntity pageEntity){
        return indexRepository.findAllByPage(pageEntity);
    }

    @Override
    @Transactional
    public void deleteIndexEntity(@NotNull List<IndexEntity> indexEntityList){
        indexEntityList.forEach(indexEntity -> indexRepository.deleteById(indexEntity.getId()));
    }

    @Override
    @Transactional
    public void addToIndexEntity(LemmaEntity lemmaEntity, Integer rank, PageEntity pageEntity) {// this IndexEntity
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemma(lemmaEntity);
        indexEntity.setLemmaRank(rank);
        indexEntity.setPage(pageEntity);
        indexRepository.saveAndFlush(indexEntity);
    }

    @Autowired
    public void setIndexRepository(IndexRepository indexRepository){
        this.indexRepository = indexRepository;
    }
}