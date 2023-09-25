package searchengine.services.statisticServices;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.services.repoServices.LemmaRepositoryService;
import searchengine.services.repoServices.PageRepositoryService;
import searchengine.services.repoServices.SiteRepositoryService;
import searchengine.services.repoServices.ServiceStore;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@RequiredArgsConstructor
public final class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private SitesList sites;


    @Autowired
    public StatisticsServiceImpl(@NotNull ServiceStore serviceStore){
        this.siteRepositoryService = serviceStore.getSiteRepositoryService();
        this.pageRepositoryService = serviceStore.getPageRepositoryService();
        this.lemmaRepositoryService = serviceStore.getLemmaRepositoryService();
    }

    @Override
    public @NotNull StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteEntity> sitesList = getAllEntity();
        sitesList.forEach(siteEntity -> {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(siteEntity.getName());
            item.setUrl(siteEntity.getUrl());
            int pages = getCountPageBySite(siteEntity);
            int lemmas = getCountLemmaBySite(siteEntity);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(siteEntity.getStatus().toString());
            item.setError(getError(siteEntity));
            item.setStatusTime(siteEntity.getStatusTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);

        });

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private List<SiteEntity> getAllEntity() {
        return siteRepositoryService.findAll();
    }

    private int getCountPageBySite(SiteEntity siteEntity) {
        return pageRepositoryService.getCountPageBySite(siteEntity);
    }

    private int getCountLemmaBySite(SiteEntity siteEntity) {
        return lemmaRepositoryService.getCountLemmaBySite(siteEntity);
    }

    private String getError(@NotNull SiteEntity siteEntity){
        if(siteEntity.getLastError() == null){
            return "";
        }
        return siteEntity.getLastError();
    }

    @Autowired
    public void setSites(SitesList sites) {
        this.sites = sites;
    }
}