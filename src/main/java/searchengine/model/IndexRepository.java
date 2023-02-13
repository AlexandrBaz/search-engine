package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndexRepository extends JpaRepository<Site, Long> {
    Optional<Site> findByNameAndUrl(String name, String url);
}
