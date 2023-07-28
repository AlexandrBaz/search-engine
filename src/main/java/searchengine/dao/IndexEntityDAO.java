//package searchengine.dao;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import searchengine.model.*;
//import searchengine.repositories.IndexRepository;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@Scope("prototype")
//@Transactional
//public class IndexEntityDAO {
//
//    IndexRepository indexRepository;
//    SiteEntityDAO siteEntityDAO;
//    PageEntityDAO pageEntityDAO;
//
//    public synchronized void addIndex(LemmaEntity lemmaEntity, Integer rank, PageEntity pageEntity) {
//        IndexEntity indexEntity = new IndexEntity();
//        indexEntity.setPage(pageEntity);
//        indexEntity.setLemmaRank(rank);
//        indexEntity.setLemma(lemmaEntity);
//        indexRepository.saveAndFlush(indexEntity);
//    }
//
////    public List<IndexEntity> getListIndexOnPage(PageEntity pageEntity){
////        return new ArrayList<>(indexRepository.findAllByPage(pageEntity));
////    }
//
////    public void deleteIndexOnPage(List<IndexEntity> indexEntityList){
////        indexEntityList.forEach(indexEntity -> {
////            indexRepository.deleteById(indexEntity.getId());
////        });
////    }
//
//    @Autowired
//    public void setPageRepository(IndexRepository indexRepository, SiteEntityDAO siteEntityDAO, PageEntityDAO pageEntityDAO) {
//        this.indexRepository = indexRepository;
//        this.siteEntityDAO = siteEntityDAO;
//        this.pageEntityDAO = pageEntityDAO;
////        this.lemmaEntityDAO = lemmaEntityDAO;
//        //, @Lazy LemmaEntityDAO lemmaEntityDAO
//    }
//}
