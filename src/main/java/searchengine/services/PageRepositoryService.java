package searchengine.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import searchengine.dto.parser.Page;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Service
public interface PageRepositoryService {

    PageEntity getPageEntity(String path, SiteEntity siteEntity);

    List<PageEntity> getPageEntityList(SiteEntity siteEntity);

    List<Long> getIdListPageEntity(SiteEntity siteEntity);

    void savePageEntity (PageEntity pageEntity);

    boolean pageEntityIsPresent(String path, String domain);

    boolean urlIsUnique(String path, String domain);

    Slice<PageEntity> getSliceOfPages(SiteEntity siteEntity, Pageable pageable);

    void addPage(Page page);

    void updatePageEntity(Page page);

    void deletePage(String path, String domain);

    void deleteByIdListPageEntity(List<Long> pageEntityListId);

    void addListPageEntity(TreeMap<String, Page> pageList, String domain);
    void addListPageEntity(List<Page> pageList, String domain);

    void addListPageEntity(List<PageEntity> pageEntityList);

    int getCountPageBySite(SiteEntity siteEntity);

    void savePageEntityMap(ConcurrentHashMap<String, PageEntity> pageEntityMap);
}
