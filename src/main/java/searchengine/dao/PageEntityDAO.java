package searchengine.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.sitehandler.ParsedPage;

import java.util.List;
import java.util.stream.Stream;

@Component
@Scope("prototype")
@Transactional
public class PageEntityDAO {
    private PageRepository pageRepository;
    private SiteEntityDAO siteEntityDAO;

    public void addUrlToTable(ParsedPage parsedPage) {
        SiteEntity siteEntity = siteEntityDAO.modifySite(parsedPage.getDomain());
        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(parsedPage.getPageStatusCode());
        pageEntity.setPath(parsedPage.getPath());
        pageEntity.setContent(parsedPage.getDocument().outerHtml());
        pageEntity.setSite(siteEntity);
        pageRepository.saveAndFlush(pageEntity);
    }

    public void upDatePageEntity(ParsedPage parsedPage) {
        SiteEntity siteEntity = siteEntityDAO.modifySite(parsedPage.getDomain());
        PageEntity pageEntity = pageRepository.findByPathAndSite(parsedPage.getPath(), siteEntityDAO.getSiteEntity(parsedPage.getDomain())).orElse(null);
        if (pageEntity != null) {
            pageEntity.setCode(parsedPage.getPageStatusCode());
            pageEntity.setContent(parsedPage.getDocument().outerHtml());
            pageEntity.setSite(siteEntity);
            pageRepository.saveAndFlush(pageEntity);
        }
    }

    public boolean pageEntityIsPresent(String url, String domain) {
        SiteEntity siteEntity = siteEntityDAO.getSiteEntity(domain);
        return pageRepository.findByPathAndSite(url, siteEntity).isPresent();
    }

    public boolean urlIsUnique(String path, String domain) {
        return pageRepository.findByPathAndSite(path, siteEntityDAO.getSiteEntity(domain)).isEmpty();
    }
    public void deleteUrl(String path, String domain){
        long id = pageRepository.findByPathAndSite(path, siteEntityDAO.getSiteEntity(domain)).orElse(null).getId();
        pageRepository.deleteById(id);
    }

    public PageEntity getPageEntity(String path, SiteEntity siteEntity){
        return pageRepository.findByPathAndSite(path, siteEntity).orElse(null);
    }

    public Slice<PageEntity> getSitePageIdSlice(SiteEntity siteEntity, Pageable pageable){
        return pageRepository.findAllBySite(siteEntity, pageable);
    }

    public Stream<PageEntity> getStreamPageEntity(SiteEntity siteEntity){
        System.out.println("start stream");
        System.out.println(pageRepository);
        return pageRepository.findAllBySite(siteEntity);
    }

    @Autowired
    public void setPageRepository(PageRepository pageRepository, SiteEntityDAO siteEntityDAO) {
        this.pageRepository = pageRepository;
        this.siteEntityDAO = siteEntityDAO;
    }
}

//    void processStudentsByFirstName(String firstName) {
//        Slice<Student> slice = repository.findAllByFirstName(firstName, PageRequest.of(0, BATCH_SIZE));
//        List<Student> studentsInBatch = slice.getContent();
//        studentsInBatch.forEach(emailService::sendEmailToStudent);
//
//        while(slice.hasNext()) {
//            slice = repository.findAllByFirstName(firstName, slice.nextPageable());
//            slice.get().forEach(emailService::sendEmailToStudent);
//        }
//    }
