package searchengine.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PageRepositoryServiceImpl implements PageRepositoryService {
    private PageRepository pageRepository;

    private SiteRepositoryService siteRepositoryService;

    @Override
    public PageEntity getPageEntity(String path, SiteEntity siteEntity) {
        return pageRepository.findByPathAndSite(path, siteEntity).orElse(null);
    }

    @Override
    public List<PageEntity> getPageEntityList(SiteEntity siteEntity) {
        return pageRepository.findAllBySite(siteEntity).collect(Collectors.toList());
    }

    @Override
    public List<Long> getIdListPageEntity(SiteEntity siteEntity) {
        List<Long> idListPageEntity = new ArrayList<>();
        pageRepository.findAllBySite(siteEntity).forEachOrdered(pageEntity -> idListPageEntity.add(pageEntity.getId()));
        return idListPageEntity;
    }

    @Override
    @Transactional
    public synchronized void savePageEntity(PageEntity pageEntity) {
        pageRepository.saveAndFlush(pageEntity);
    }

    @Override
    public boolean pageEntityIsPresent(String path, String domain) {
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(domain);
        return pageRepository.findByPathAndSite(path, siteEntity).isPresent();
    }

    @Override
    public boolean urlIsUnique(String path, String domain) {
        return pageRepository.findByPathAndSite(path, siteRepositoryService.getSiteEntityByDomain(domain)).isEmpty();
    }

    @Override
    public Slice<PageEntity> getSliceOfPages(SiteEntity siteEntity, Pageable pageable) {
        return pageRepository.findAllBySite(siteEntity, pageable);
    }
    @Override
    @Transactional
    public void deletePage(PageEntity pageEntity) {
        pageRepository.delete(pageEntity);

    }
    @Override
    @Transactional
    public void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        pageRepository.deleteAllByIdInBatch(pageEntityListId);
    }

    @Override
    @Transactional
    public synchronized void addListPageEntity(@NotNull List<PageEntity> pageEntityList) {
        pageRepository.saveAllAndFlush(pageEntityList);
    }

    @Override
    public int getCountPageBySite(SiteEntity siteEntity) {
        return pageRepository.countBySite(siteEntity);
    }

    @Override
    @Transactional
    public synchronized void savePageEntityMap(@NotNull ConcurrentHashMap<String, PageEntity> pageEntityMap) {
        List<PageEntity> pageEntityList = pageEntityMap.values().stream().toList();
        System.out.println(pageEntityList.size() + " before write " + pageEntityMap.size());
        pageRepository.saveAllAndFlush(pageEntityList);
    }

    @Override
    public PageEntity getPageEntityByID(long pageId) {
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
