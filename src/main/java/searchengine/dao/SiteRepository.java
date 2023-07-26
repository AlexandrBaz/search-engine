package searchengine.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

import java.util.Optional;
@Repository
@Transactional
@Scope("prototype")
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    Optional<SiteEntity> findByUrl(String url);
}
