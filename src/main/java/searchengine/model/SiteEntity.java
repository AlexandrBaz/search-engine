package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site")
@Getter
@Setter
public class SiteEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "site_id",nullable = false, unique = true)
    private Long Id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    @Column(columnDefinition = "varchar(255)", unique = true)
    private String url;
    @Column(columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, targetEntity = PageEntity.class,
            orphanRemoval = true)
    private List<PageEntity> pageEntities = new ArrayList<>();

    public void addPage(PageEntity pageEntity) {
        pageEntities.add(pageEntity);
        pageEntity.setSite(this);
    }

    public void removePage(PageEntity pageEntity) {
        pageEntities.remove(pageEntity);
        pageEntity.setSite(null);
    }
}
