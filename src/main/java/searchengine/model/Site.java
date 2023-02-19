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
public class Site extends searchengine.config.Site {
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

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Page> pages = new ArrayList<>();

    public void addPage(Page page) {
        pages.add(page);
        page.setSite(this);
    }

    public void removePage(Page page) {
        pages.remove(page);
        page.setSite(null);
    }
}
