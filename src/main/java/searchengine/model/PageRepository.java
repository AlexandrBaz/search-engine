package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PageRepository extends JpaRepository<PageEntity, Long> {

    Optional<PageEntity> findByPathAndSite(String path, SiteEntity site);
}
