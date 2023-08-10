package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;
@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    Optional<LemmaEntity> findByLemmaAndSite(String lemma, SiteEntity siteEntity);

    List<LemmaEntity> findAllBySite(SiteEntity siteEntity);

    int countBySite(SiteEntity siteEntity);
}
