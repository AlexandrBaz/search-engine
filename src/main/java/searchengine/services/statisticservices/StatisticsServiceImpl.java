package searchengine.services.statisticservices;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.services.reposervices.LemmaRepositoryService;
import searchengine.services.reposervices.PageRepositoryService;
import searchengine.services.reposervices.SiteRepositoryService;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter
public final class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepositoryService siteRepositoryService;
    private final PageRepositoryService pageRepositoryService;
    private final LemmaRepositoryService lemmaRepositoryService;
    private final SitesList sites;

    @Autowired
    public StatisticsServiceImpl(SiteRepositoryService siteRepositoryService, PageRepositoryService pageRepositoryService,
                                 LemmaRepositoryService lemmaRepositoryService, SitesList sites) {
        this.siteRepositoryService = siteRepositoryService;
        this.pageRepositoryService = pageRepositoryService;
        this.lemmaRepositoryService = lemmaRepositoryService;
        this.sites = sites;
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
}