package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {

    List<IndexEntity> findAllByPage(PageEntity pageEntity);

}
