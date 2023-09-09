package searchengine.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.utils.ServiceStore;
import searchengine.utils.parser.PageParser;

@Service
public class IndexServiceAsyncImpl implements IndexServiceAsync{
    private ServiceStore serviceStore;
    @Override
    @Async
    public void parsePage(String url, @NotNull Site site) {
        System.out.println(serviceStore);
        SiteRepositoryService siteRepositoryService = serviceStore.getSiteRepositoryService();
        SiteEntity siteEntity = siteRepositoryService.getSiteEntityByDomain(site.getUrl());
        if (siteEntity == null){
            siteRepositoryService.createSite(site, Status.INDEXED);
        }
        PageParser pageParser = new PageParser(url, site.getUrl(), serviceStore);
        pageParser.addOrUpdatePage();
    }

    @Autowired
    public void setServiceStore(ServiceStore serviceStore){this.serviceStore = serviceStore;}
}
