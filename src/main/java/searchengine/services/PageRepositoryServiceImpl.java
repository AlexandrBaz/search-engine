package searchengine.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.parser.Page;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
        pageRepository.findAllBySite(siteEntity).forEachOrdered(pageEntity -> {
            idListPageEntity.add(pageEntity.getId());
        });
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
    public void updatePageEntity(@NotNull Page page) {
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
    public void deletePage(String path, String domain) {
        long id = getPageEntity(path, siteRepositoryService.getSiteEntityByDomain(domain)).getId();
        pageRepository.deleteById(id);
    }
    @Override
    @Transactional
    public void deleteByIdListPageEntity(List<Long> pageEntityListId) {
        pageRepository.deleteAllById(pageEntityListId);
    }

    @Override
    @Transactional
    public synchronized void addListPageEntity(@NotNull List<Page> pageList, String domain) {
        SiteEntity siteEntity = siteRepositoryService.updateSiteEntity(domain);
        CopyOnWriteArrayList<PageEntity> pageEntityList = getConvertedPageToPageEntity(pageList, siteEntity);
        pageRepository.saveAll(pageEntityList);
    }

    @Override
    @Transactional
    public synchronized void addListPageEntity(@NotNull List<PageEntity> pageEntityList) {
        System.out.println(pageEntityList.size() + " before adding");
        pageRepository.saveAll(pageEntityList);
    }

    private @NotNull CopyOnWriteArrayList<PageEntity> getConvertedPageToPageEntity(@NotNull List<Page> pageList, SiteEntity siteEntity) {
        CopyOnWriteArrayList<PageEntity> pageEntityList = new CopyOnWriteArrayList<>();
        pageList.forEach(page -> {
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSite(siteEntity);
            pageEntity.setPath(page.getPath());
            pageEntity.setCode(page.getPageStatusCode());
            pageEntity.setContent(page.getDocument().outerHtml());
            synchronized (pageEntityList) {
                pageEntityList.add(pageEntity);
            }
        });
        System.out.println(pageList.size() + " after converting " + pageEntityList.size());
        return pageEntityList;
    }

    @Override
    @Transactional
    public synchronized void addListPageEntity(@NotNull TreeMap<String, Page> pageList, String domain) {
        System.out.println("start adding");
        SiteEntity siteEntity = siteRepositoryService.updateSiteEntity(domain);
        List<PageEntity> pageEntityList = new ArrayList<>();
        pageList.forEach((k, v) -> {
            pageEntityList.add(convertPageToPageEntity(v, siteEntity));
//            pageRepository.save(convertPageToPageEntity(v, siteEntity));
        });
        System.out.println(pageEntityList.size() + " from addListPageEntity " + Thread.currentThread().getName());
        pageRepository.saveAll(pageEntityList);
        System.out.println(pageEntityList.size() + " end addListPageEntity " + Thread.currentThread().getName());

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
        pageRepository.saveAll(pageEntityList);
    }

    @Autowired
    public void setPageRepository(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService) {
        this.siteRepositoryService = siteRepositoryService;
    }

    private synchronized @NotNull PageEntity convertPageToPageEntity(@NotNull Page page, SiteEntity siteEntity) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setSite(siteEntity);
        pageEntity.setPath(page.getPath());
        pageEntity.setCode(page.getPageStatusCode());
        pageEntity.setContent(page.getDocument().outerHtml());
        return pageEntity;
    }
}
