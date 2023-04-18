package searchengine.model;

import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
@Scope("prototype")
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    Optional<PageEntity> findByPathAndSite(String path, SiteEntity site);
}
