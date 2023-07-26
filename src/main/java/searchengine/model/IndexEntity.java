package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "`index`")
//, indexes = {@Index(name = "index_key", columnList="lemma_id, page_id", unique = true)}
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    PageEntity page;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false)
    LemmaEntity lemma;
    @Column(name = "`rank`",nullable = false)
    private float lemmaRank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntity that = (IndexEntity) o;
        return id == that.id && Float.compare(that.lemmaRank, lemmaRank) == 0 && page.equals(that.page) && lemma.equals(that.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, page, lemma, lemmaRank);
    }
}
