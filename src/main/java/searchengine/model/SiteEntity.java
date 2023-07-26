package searchengine.model;

import lombok.*;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "site")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
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

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, targetEntity = PageEntity.class,
            orphanRemoval = true)
    private List<LemmaEntity> lemmaEntities = new ArrayList<>();

    public void addPage(PageEntity pageEntity) {
        pageEntities.add(pageEntity);
        pageEntity.setSite(this);
    }

    public void removePage(PageEntity pageEntity) {
        pageEntities.remove(pageEntity);
        pageEntity.setSite(null);
    }

    public void addLemma(LemmaEntity lemmaEntity) {
        lemmaEntities.add(lemmaEntity);
        lemmaEntity.setSite(this);
    }

    public void removeLemma(LemmaEntity lemmaEntity) {
        lemmaEntities.remove(lemmaEntity);
        lemmaEntity.setSite(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteEntity that = (SiteEntity) o;
        return Id.equals(that.Id) && status == that.status && statusTime.equals(that.statusTime) && lastError.equals(that.lastError) && url.equals(that.url) && name.equals(that.name) && pageEntities.equals(that.pageEntities) && lemmaEntities.equals(that.lemmaEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, status, statusTime, lastError, url, name, pageEntities, lemmaEntities);
    }
}
