package searchengine.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "lemma")
//, indexes = {@Index(name = "index_key", columnList="lemma, site_id", unique = true)}
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    SiteEntity site;
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String lemma;
    @Column(nullable = false)
    private long frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL, targetEntity = IndexEntity.class, orphanRemoval = true)
    private List<IndexEntity> indexLemmaEntities = new ArrayList<>();

    public void addIndex(IndexEntity indexEntity) {
        indexLemmaEntities.add(indexEntity);
        indexEntity.setLemma(this);
    }

    public void removeIndex(IndexEntity indexEntity) {
        indexLemmaEntities.remove(indexEntity);
        indexEntity.setLemma(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LemmaEntity that = (LemmaEntity) o;
        return id == that.id && frequency == that.frequency && site.equals(that.site) && lemma.equals(that.lemma) && indexLemmaEntities.equals(that.indexLemmaEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, site, lemma, frequency, indexLemmaEntities);
    }
}
