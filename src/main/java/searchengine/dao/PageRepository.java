package searchengine.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Transactional
@Scope("prototype")
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    Optional<PageEntity> findByPathAndSite(String path, SiteEntity siteEntity);

    Slice<PageEntity> findAllBySite(SiteEntity siteEntity, Pageable pageable);

    Stream<PageEntity> findAllBySite(SiteEntity siteEntity);
}
