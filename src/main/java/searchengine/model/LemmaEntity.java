package searchengine.model;

import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "lemma", indexes = {@Index(name = "index_key", columnList="lemma, site_id", unique = true)})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    SiteEntity site;
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String lemma;
    @Column(nullable = false)
    private long frequency;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "lemma", targetEntity = IndexEntity.class)
    private List<IndexEntity> indexLemmaEntities = new ArrayList<>();

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
