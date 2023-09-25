package searchengine.model;

import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "site", targetEntity = PageEntity.class)
    private List<PageEntity> pageEntities = new ArrayList<>();
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "site", targetEntity = PageEntity.class)
    private List<LemmaEntity> lemmaEntities = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteEntity that = (SiteEntity) o;
        return Id.equals(that.Id) && status == that.status && statusTime.equals(that.statusTime) &&
//                lastError.equals(that.lastError) &&
                url.equals(that.url) && name.equals(that.name) && pageEntities.equals(that.pageEntities) && lemmaEntities.equals(that.lemmaEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, status, statusTime, lastError, url, name, pageEntities, lemmaEntities);
    }
}
