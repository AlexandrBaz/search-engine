package searchengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci", name = "content")
    private String content;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "page", targetEntity = IndexEntity.class)
    private List<IndexEntity> indexPageEntities = new ArrayList<>();

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
