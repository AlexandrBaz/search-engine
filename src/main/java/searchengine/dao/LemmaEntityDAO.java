//package searchengine.dao;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import searchengine.model.IndexEntity;
//import searchengine.model.LemmaEntity;
//import searchengine.model.PageEntity;
//import searchengine.model.SiteEntity;
//import searchengine.repositories.LemmaRepository;
//import searchengine.services.LemmaRepositoryServiceImpl;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//@Component
//@Scope("prototype")
//@Transactional
//public class LemmaEntityDAO {
//    private LemmaRepository lemmaRepository;
//    private IndexEntityDAO indexEntityDAO;
//
//    private LemmaRepositoryServiceImpl lemmaRepositoryService;
//
////    public synchronized void addNewLemma(Map<String, Integer> lemmaMap, PageEntity pageEntity) {
////        lemmaMap.forEach((lemma, rank) -> {
////            LemmaEntity lemmaIsPresented = lemmaRepositoryService.getLemmaEntity(lemma, pageEntity.getSite());
////            if (lemmaIsPresented != null) {
////                lemmaIsPresented.setFrequency(lemmaIsPresented.getFrequency() + 1);
////                lemmaRepository.saveAndFlush(lemmaIsPresented);
////            } else {
////                LemmaEntity lemmaEntity = new LemmaEntity();
////                lemmaEntity.setLemma(lemma);
////                lemmaEntity.setFrequency(1);
////                lemmaEntity.setSite(pageEntity.getSite());
////                lemmaRepository.saveAndFlush(lemmaEntity);
////            }
////            addToIndexEntity(lemma, rank, pageEntity);
////        });
////    }
//
////    private void addToIndexEntity(String lemma, Integer rank, PageEntity pageEntity) {
////        SiteEntity siteEntity = pageEntity.getSite();
////        LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSite(lemma, siteEntity).orElse(null);
////        indexEntityDAO.addIndex(lemmaEntity, rank, pageEntity);
////    }
//
////    public void deleteLemmaOnPage(List<IndexEntity> indexEntityList) {
////        indexEntityList.forEach(indexEntity -> {
////            LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSite(indexEntity.getLemma().getLemma(), indexEntity.getLemma().getSite()).orElse(null);
////            if (Objects.requireNonNull(lemmaEntity).getFrequency() == 1) {
////                lemmaRepository.deleteById(indexEntity.getLemma().getId());
////            } else {
////                lemmaEntity.setFrequency(lemmaEntity.getFrequency() - 1);
////                lemmaRepository.saveAndFlush(lemmaEntity);
////            }
////        });
////    }
//
////    private LemmaEntity getLemmaEntity(String lemma, SiteEntity siteEntity) {
////        return lemmaRepository.findByLemmaAndSite(lemma, siteEntity).orElse(null);
////    }
//
//    @Autowired
//    public void setPageRepository(LemmaRepository lemmaRepository, IndexEntityDAO indexEntityDAO) {
//        this.lemmaRepository = lemmaRepository;
//        this.indexEntityDAO = indexEntityDAO;
//    }
//
//    @Autowired
//    public void setLemmaRepository(LemmaRepositoryServiceImpl lemmaRepository){
//        this.lemmaRepositoryService = lemmaRepositoryService;
//    }
//
//}
