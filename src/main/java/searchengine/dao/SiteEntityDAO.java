//package searchengine.dao;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import searchengine.repositories.SiteRepository;
//import searchengine.model.SiteEntity;
//import searchengine.model.Status;
//
//import java.time.LocalDateTime;
//
//@Component
//@Scope("prototype")
//@Transactional
//public class SiteEntityDAO {
//
//    public SiteRepository siteRepository;
//
////    public synchronized void createSite(searchengine.config.Site siteToIndex) {
////        boolean siteIsPresent = siteRepository.findByUrl(siteToIndex.getUrl()).isPresent();
////        if (!siteIsPresent) {
////            SiteEntity addSite = new SiteEntity();
////            addSite.setName(siteToIndex.getName());
////            addSite.setUrl(siteToIndex.getUrl());
////            addSite.setStatus(Status.INDEXING);
////            addSite.setStatusTime(LocalDateTime.now());
////            addSite.setLastError(null);
////            siteRepository.saveAndFlush(addSite);
////        }
////    }
////    public SiteEntity modifySite(String domain) {
////        SiteEntity modifySite = getSiteEntity(domain);
////        modifySite.setStatusTime(LocalDateTime.now());
////        siteRepository.saveAndFlush(modifySite);
////        return modifySite;
////    }
//
////    public void setParseError(SiteEntity getSiteFromTable, String error) { //TODO May be needed in future
////        getSiteFromTable.setStatusTime(LocalDateTime.now());
////        getSiteFromTable.setLastError(error);
////        siteRepository.saveAndFlush(getSiteFromTable);
////    }
////
////    public SiteEntity getSiteEntity(String domain) {
////        return siteRepository.findByUrl(domain).orElse(null);
////    }
////
////    public void stopIndexEntity(String domain) {
////        SiteEntity siteEntity = siteRepository.findByUrl(domain).orElse(null);
////        assert siteEntity != null;
////        siteEntity.setLastError("Индексация остановлена пользователем");
////        siteEntity.setStatusTime(LocalDateTime.now());
////        siteEntity.setStatus(Status.FAILED);
////        siteRepository.saveAndFlush(siteEntity);
////    }
//
////    public void indexComplete(String url) {
////        SiteEntity siteEntity = siteRepository.findByUrl(url).orElse(null);
////        if (siteEntity != null) {
////            siteEntity.setStatus(Status.INDEXED);
////            siteEntity.setLastError(null);
////            siteEntity.setStatusTime(LocalDateTime.now());
////            siteRepository.saveAndFlush(siteEntity);
////        }
////    }
//    @Autowired
//    public void setSiteRepository(SiteRepository siteRepository){
//        this.siteRepository = siteRepository;
//    }
//}
