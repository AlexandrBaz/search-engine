//package searchengine.dao;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import searchengine.model.PageEntity;
//import searchengine.model.SiteEntity;
//import searchengine.repositories.PageRepository;
//import searchengine.services.SiteRepositoryService;
//import searchengine.utils.Parser.ParsedPage;
//
//import java.util.stream.Stream;
//
//@Component
//@Scope("prototype")
//@Transactional
//public class PageEntityDAO {
//    private PageRepository pageRepository;
//    private SiteEntityDAO siteEntityDAO;
//
//    private SiteRepositoryService siteRepositoryService;
//
////    public void addUrlToTable(ParsedPage parsedPage) {
////        SiteEntity siteEntity = siteRepositoryService.modifySiteEntity(parsedPage.getDomain());
////        PageEntity pageEntity = new PageEntity();
////        pageEntity.setCode(parsedPage.getPageStatusCode());
////        pageEntity.setPath(parsedPage.getPath());
////        pageEntity.setContent(parsedPage.getDocument().outerHtml());
////        pageEntity.setSite(siteEntity);
////        pageRepository.saveAndFlush(pageEntity);
////    }
//
////    public void upDatePageEntity(ParsedPage parsedPage) {
////        SiteEntity siteEntity = siteRepositoryService.updateSiteEntity(parsedPage.getDomain());
////        PageEntity pageEntity = pageRepository.findByPathAndSite(parsedPage.getPath(), siteRepositoryService.getSiteEntityByDomain(parsedPage.getDomain())).orElse(null);
////        if (pageEntity != null) {
////            pageEntity.setCode(parsedPage.getPageStatusCode());
////            pageEntity.setContent(parsedPage.getDocument().outerHtml());
////            pageEntity.setSite(siteEntity);
////            pageRepository.saveAndFlush(pageEntity);
////        }
////    }
//
////    public boolean pageEntityIsPresent(String url, String domain) {
////        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
////        return pageRepository.findByPathAndSite(url, siteEntity).isPresent();
////    }
//
////    public boolean urlIsUnique(String path, String domain) {
////        return pageRepository.findByPathAndSite(path, siteRepositoryService.getSiteEntityByDomain(domain)).isEmpty();
////    }
////    public void deleteUrl(String path, String domain){
////        long id = pageRepository.findByPathAndSite(path, siteRepositoryService.getSiteEntityByDomain(domain)).orElse(null).getId();
////        pageRepository.deleteById(id);
////    }
//
////    public PageEntity getPageEntity(String path, SiteEntity siteEntity){
////        return pageRepository.findByPathAndSite(path, siteEntity).orElse(null);
////    }
////
////    public Slice<PageEntity> getSitePageIdSlice(SiteEntity siteEntity, Pageable pageable){
////        return pageRepository.findAllBySite(siteEntity, pageable);
////    }
//
//    public Stream<PageEntity> getStreamPageEntity(SiteEntity siteEntity){
//        System.out.println("start stream");
//        System.out.println(pageRepository);
//        return pageRepository.findAllBySite(siteEntity);
//    }
//
//    @Autowired
//    public void setPageRepository(PageRepository pageRepository, SiteEntityDAO siteEntityDAO, SiteRepositoryService siteRepositoryService) {
//        this.pageRepository = pageRepository;
//        this.siteEntityDAO = siteEntityDAO;
//        this.siteRepositoryService = siteRepositoryService;
//    }
//}
