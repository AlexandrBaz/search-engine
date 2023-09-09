package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {

    List<IndexEntity> findAllByPage(PageEntity pageEntity);

    List<IndexEntity> findByLemma(LemmaEntity lemmaEntity);

    Optional<IndexEntity> findByLemmaAndPage(LemmaEntity lemmaEntity, PageEntity pageEntity);
}
