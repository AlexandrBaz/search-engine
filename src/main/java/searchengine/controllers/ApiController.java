package searchengine.controllers;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.index.FalseResponse;
import searchengine.dto.index.Response;
import searchengine.dto.index.TrueResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexServices.IndexService;
import searchengine.services.searchServices.SearchService;
import searchengine.services.statisticServices.StatisticsService;


@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexService indexService;
    private final SearchService searchService;
    private Response response;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexService indexService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexService = indexService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public Response startIndexing(){
        if (indexService.startIndexing()){
            response = new TrueResponse(true);
        } else {
            response = new FalseResponse(false, "Индексация уже запущена");
        }
        return response;
    }

    @GetMapping("/stopIndexing")
    public Response stopIndexing(){
        if (indexService.stopIndexing()){
            response = new TrueResponse(true);
        } else response = new FalseResponse(false, "Индексация не запущена");
        return response;
    }

    @PostMapping("/indexPage")
    public Response indexPage(@RequestParam (name="url") String url){
        if(indexService.indexPage(url)){
            response = new TrueResponse(true);
        } else {
            response = new FalseResponse(false,"Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return response;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchQuery(@RequestParam (name = "query") @NotNull String query,
                                                      @RequestParam (name = "site", defaultValue = "all") String site,
                                                      @RequestParam (name = "offset", defaultValue = "0") Integer offset,
                                                      @RequestParam (name = "limit", defaultValue = "10") Integer limit) {

        SearchResponse searchResponse;
        if (!query.isBlank()){
            searchResponse = searchService.getPages(query, site, offset, limit);

        } else {
            return ResponseEntity.ok(new FalseResponse(false, "Задан пустой поисковый запрос"));
        }
        return ResponseEntity.ok(searchResponse);

    }
}
