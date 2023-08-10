package searchengine.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import searchengine.dto.parser.Page;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.TreeMap;
import java.util.TreeSet;

@Service
public interface PageRepositoryService {

    PageEntity getPageEntity(String path, SiteEntity siteEntity);

    boolean pageEntityIsPresent(String url, String domain);

    boolean urlIsUnique(String path, String domain);

    Slice<PageEntity> getSliceOfPages(SiteEntity siteEntity, Pageable pageable);

    void addPage(Page page);

    void updatePageEntity(Page page);

    void deletePage(String path, String domain);

    void addListPageEntity(TreeMap<String, Page> pageList, String domain);

    int getCountPageBySite(SiteEntity siteEntity);
}
