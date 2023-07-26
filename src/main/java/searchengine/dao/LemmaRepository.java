package searchengine.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.Optional;
@Repository
@Transactional
@Scope("prototype")
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    Optional<LemmaEntity> findByLemmaAndSite(String lemma, SiteEntity siteEntity);
}
