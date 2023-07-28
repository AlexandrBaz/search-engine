package searchengine.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.model.SiteEntity;

@Service
@Scope("prototype")
public interface SiteRepositoryService {

   SiteEntity getSiteEntityByDomain(String domain);

   void createSite(Site site);

   SiteEntity updateSiteEntity(String domain);

   void stopIndexingThisEntity(String domain);

   void siteIndexComplete(String domain);

   void setParseError(SiteEntity siteEntity, String error);
}
