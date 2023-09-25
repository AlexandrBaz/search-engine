package searchengine.services.repoServices;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service("SiteRepositoryServiceImpl")
@Transactional(readOnly = true)
public class SiteRepositoryServiceImpl implements SiteRepositoryService {

    private SiteRepository siteRepository;

    @Override
    public SiteEntity getSiteEntityByDomain(String domain) {
        return siteRepository.findByUrl(domain).orElse(null);
    }

    @Override
    public List<SiteEntity> findAll() {
        return siteRepository.findAll();
    }

    @Override
    @Transactional
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
    @Transactional
    public synchronized void updateSite(@NotNull SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    @Override
    @Transactional
    public void deleteSiteEntity(SiteEntity siteEntity) {
        siteRepository.delete(siteEntity);
    }

    @Override
    @Transactional
    public void stopIndexingThisEntity(String domain) {
        SiteEntity siteEntity = getSiteEntityByDomain(domain);
        siteEntity.setLastError("Индексация остановлена пользователем");
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setStatus(Status.FAILED);
        siteRepository.saveAndFlush(siteEntity);
    }

    @Override
    @Transactional
    public void siteIndexComplete(String domain) {
        SiteEntity siteEntity = getSiteEntityByDomain(domain);
        if (siteEntity != null) {
            siteEntity.setStatus(Status.INDEXED);
            siteEntity.setLastError(null);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.saveAndFlush(siteEntity);
        }
    }

    @Override
    @Transactional
    public void setParseError(@NotNull SiteEntity siteEntity, String error) {
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
