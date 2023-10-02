package searchengine.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    Optional<PageEntity> findByPathAndSite(String path, SiteEntity siteEntity);

    Slice<PageEntity> findAllBySite(SiteEntity siteEntity, Pageable pageable);

    int countBySite(SiteEntity siteEntity);

    List<PageEntity> findAllBySite(SiteEntity siteEntity);
}
