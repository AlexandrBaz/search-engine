package searchengine.services.reposervices;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service()
public class SiteRepositoryServiceImpl implements SiteRepositoryService {

    private SiteRepository siteRepository;

    @Override
    public synchronized SiteEntity getSiteEntityByDomain(String domain) {
        return siteRepository.findByUrl(domain).orElse(null);
    }

    @Override
    public synchronized List<SiteEntity> findAll() {
        return siteRepository.findAll();
    }

    @Override
    public synchronized void createSite(@NotNull Site site, Status status) {
        if (getSiteEntityByDomain(site.getUrl()) == null) {
            SiteEntity newSiteEntity = new SiteEntity();
            newSiteEntity.setName(site.getName());
            newSiteEntity.setUrl(site.getUrl());
            newSiteEntity.setStatus(status);
            newSiteEntity.setStatusTime(LocalDateTime.now());
            newSiteEntity.setLastError(null);
            siteRepository.saveAndFlush(newSiteEntity);
        }
    }

    @Override
    public synchronized void updateSite(@NotNull SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    @Override
    public synchronized void deleteSiteEntity(SiteEntity siteEntity) {
        siteRepository.delete(siteEntity);
    }

    @Override
    public synchronized void stopIndexingThisEntity(String domain) {
        SiteEntity siteEntity = getSiteEntityByDomain(domain);
        siteEntity.setLastError("Индексация остановлена пользователем");
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(Status.FAILED);
        siteRepository.saveAndFlush(siteEntity);
    }

    @Override
    public synchronized void siteIndexComplete(String domain) {
        SiteEntity siteEntity = getSiteEntityByDomain(domain);
        if (siteEntity != null) {
            siteEntity.setStatus(Status.INDEXED);
            siteEntity.setLastError(null);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(siteEntity);
        }
    }

    @Override
    public synchronized void setParseError(@NotNull SiteEntity siteEntity, String error) {
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setLastError(error);
        siteRepository.saveAndFlush(siteEntity);
    }

    @Override
    public List<SiteEntity> getSiteByStatus(Status indexed) {
        return siteRepository.findByStatus(indexed);
    }

    @Autowired
    public void setSiteRepository(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }
}