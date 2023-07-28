package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.parser.Page;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;

@Service
@Transactional(readOnly = true)
public class PageRepositoryServiceImpl implements PageRepositoryService{
    private PageRepository pageRepository;

    private SiteRepositoryService siteRepositoryService;
    @Override
    public PageEntity getPageEntity(String path, SiteEntity siteEntity){
        return pageRepository.findByPathAndSite(path, siteEntity).orElse(null);
    }

    @Override
    public boolean pageEntityIsPresent(String url, String domain) {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
        return pageRepository.findByPathAndSite(url, siteEntity).isPresent();
    }

    @Override
    public boolean urlIsUnique(String path, String domain) {
        return pageRepository.findByPathAndSite(path, siteRepositoryService.getSiteEntityByDomain(domain)).isEmpty();
    }

    @Override
    public Slice<PageEntity> getSliceOfPages(SiteEntity siteEntity, Pageable pageable){
        return pageRepository.findAllBySite(siteEntity, pageable);
    }

    @Override
    @Transactional
    public void addPage(Page page) {
        SiteEntity siteEntity = siteRepositoryService.updateSiteEntity(page.getDomain());
        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(page.getPageStatusCode());
        pageEntity.setPath(page.getPath());
        pageEntity.setContent(page.getDocument().outerHtml());
        pageEntity.setSite(siteEntity);
        pageRepository.saveAndFlush(pageEntity);
    }

    @Override
    @Transactional
    public void updatePageEntity(Page page) {
        SiteEntity siteEntity = siteRepositoryService.updateSiteEntity(page.getDomain());
        PageEntity pageEntity = getPageEntity(page.getPath(), siteEntity);
        if (pageEntity != null) {
            pageEntity.setCode(page.getPageStatusCode());
            pageEntity.setContent(page.getDocument().outerHtml());
            pageEntity.setSite(siteEntity);
            pageRepository.saveAndFlush(pageEntity);
        }
    }

    @Override
    @Transactional
    public void deletePage(String path, String domain){
        long id = getPageEntity(path, siteRepositoryService.getSiteEntityByDomain(domain)).getId();
        pageRepository.deleteById(id);
    }

    @Autowired
    public void setPageRepository(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService){
        this.siteRepositoryService = siteRepositoryService;
    }
}
