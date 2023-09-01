package searchengine.model;

import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "page", indexes = {@Index(name = "path_index", columnList="site_id, path", unique = true)})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PageEntity {
    @Id
//    @GenericGenerator(
//            name = "sequenceGenerator",
////            type = "enhanced-sequence",
//            type = org.hibernate.id.enhanced.SequenceStyleGenerator.class,
//            parameters = {
//                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "hibernate_sequence"),
//                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
//                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
//                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "50")
//            }
//    )
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "sequenceGenerator"
//    )
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long Id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    SiteEntity site;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;
    @Column(columnDefinition = "INT", nullable = false)
    private Integer code;

//    @Column(length = 999999, name = "content", nullable = true)
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci", name = "content", nullable = true)
    private String content;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "page", targetEntity = IndexEntity.class)
    private List<IndexEntity> indexPageEntities = new ArrayList<>();

    public void addIndex(IndexEntity indexEntity) {
        indexPageEntities.add(indexEntity);
        indexEntity.setPage(this);
    }

    public void removeIndex(IndexEntity indexEntity) {
        indexPageEntities.remove(indexEntity);
        indexEntity.setPage(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageEntity that = (PageEntity) o;
        return Id.equals(that.Id) && site.equals(that.site) && path.equals(that.path) && code.equals(that.code) && content.equals(that.content) && indexPageEntities.equals(that.indexPageEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, site, path, code, content, indexPageEntities);
    }
}
