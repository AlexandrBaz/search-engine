package searchengine.dao;

import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@Scope("prototype")
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {

    List<IndexEntity> findAllByPage(PageEntity pageEntity);
}
