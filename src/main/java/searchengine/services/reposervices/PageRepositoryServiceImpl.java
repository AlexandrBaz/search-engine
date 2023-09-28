package searchengine.services.reposervices;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PageRepositoryServiceImpl implements PageRepositoryService {
    private PageRepository pageRepository;
    private SiteRepositoryService siteRepositoryService;

    @Override
    public synchronized PageEntity getPageEntity(String path, SiteEntity siteEntity) {
        return pageRepository.findByPathAndSite(path, siteEntity).orElse(null);
    }

    @Override
    public synchronized List<Long> getIdListPageEntity(SiteEntity siteEntity) {
        List<Long> idListPageEntity = new ArrayList<>();
        pageRepository.findAllBySite(siteEntity).forEachOrdered(pageEntity -> idListPageEntity.add(pageEntity.getId()));
        return idListPageEntity;
    }

    @Override
    public synchronized void savePageEntity(PageEntity pageEntity) {
        pageRepository.saveAndFlush(pageEntity);
    }

    @Override
    public synchronized boolean pageEntityIsPresent(String path, String domain) {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
        return pageRepository.findByPathAndSite(path, siteEntity).isPresent();
    }

    @Override
    public synchronized Slice<PageEntity> getSliceOfPages(SiteEntity siteEntity, Pageable pageable) {
        return pageRepository.findAllBySite(siteEntity, pageable);
    }
    @Override
    public synchronized void deletePage(PageEntity pageEntity) {
        pageRepository.delete(pageEntity);

    }
    @Override
    public synchronized void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        pageRepository.deleteAllByIdInBatch(pageEntityListId);
    }

    @Override
    public synchronized void addListPageEntity(@NotNull List<PageEntity> pageEntityList) {
        pageRepository.saveAllAndFlush(pageEntityList);
    }

    @Override
    public synchronized int getCountPageBySite(SiteEntity siteEntity) {
        return pageRepository.countBySite(siteEntity);
    }

    @Override
    public synchronized void savePageEntityMap(@NotNull Map<String, PageEntity> pageEntityMap) {
        List<PageEntity> pageEntityList = pageEntityMap.values().stream().toList();
        pageRepository.saveAllAndFlush(pageEntityList);
    }

    @Override
    public synchronized PageEntity getPageEntityByID(long pageId) {
        return pageRepository.findById(pageId).orElse(null);
    }

    @Autowired
    public void setPageRepository(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService) {
        this.siteRepositoryService = siteRepositoryService;
    }
}
