package searchengine.services;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.model.PageEntity;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;

@Component
@Scope("prototype")
public class PageEntityDAO {
    private PageRepository pageRepository;
    private SiteEntityDAO siteEntityDAO;

    public void addUrlToTable(int urlStatusCode, Document doc, String path, String domain) {
        SiteEntity siteEntity = siteEntityDAO.modifySite(domain);
        PageEntity pageEntity = new PageEntity();
        pageEntity.setCode(urlStatusCode);
        pageEntity.setPath(path);
        pageEntity.setContent(doc.toString());
        pageEntity.setSite(siteEntity);
        pageRepository.save(pageEntity);
    }

    public void upDatePageEntity(String url, String domain, int code, Document document) {
        SiteEntity siteEntity = siteEntityDAO.modifySite(domain);
        PageEntity pageEntity = pageRepository.findByPathAndSite(url, siteEntityDAO.getSiteEntity(domain)).orElse(null);
        if (pageEntity != null) {
            pageEntity.setCode(code);
            pageEntity.setContent(document.toString());
            pageEntity.setSite(siteEntity);
            pageRepository.saveAndFlush(pageEntity);
        }
    }

    public boolean pageEntityIsPresent(String url, String domain) {
        SiteEntity siteEntity = siteEntityDAO.getSiteEntity(domain);
        System.out.println(pageRepository.findByPathAndSite(url, siteEntity));
        return pageRepository.findByPathAndSite(url, siteEntity).isPresent();
    }

    public boolean urlIsUnique(String path, String domain) {
        return pageRepository.findByPathAndSite(path, siteEntityDAO.getSiteEntity(domain)).isPresent();
    }

    @Autowired
    public void setPageRepository(PageRepository pageRepository, SiteEntityDAO siteEntityDAO) {
        this.pageRepository = pageRepository;
        this.siteEntityDAO = siteEntityDAO;
    }
}
