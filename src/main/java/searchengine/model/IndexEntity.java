package searchengine.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


import java.util.Objects;

@Entity
@Table(name = "`index`" )
//, indexes = {@Index(name = "index_key", columnList="lemma_id, page_id", unique = true)}
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private long id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    PageEntity page;
    @OnDelete(action = OnDeleteAction.CASCADE)
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
