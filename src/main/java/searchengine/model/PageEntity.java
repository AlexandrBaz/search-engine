package searchengine.model;

import lombok.*;

import javax.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long Id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    SiteEntity site;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;
    @Column(columnDefinition = "INT", nullable = false)
    private Integer code;
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, targetEntity = IndexEntity.class, orphanRemoval = true)
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
