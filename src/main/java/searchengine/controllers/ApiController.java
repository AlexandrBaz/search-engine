package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.index.FalseResponse;
import searchengine.dto.index.Response;
import searchengine.dto.index.TrueResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexService indexService;
    private Response response;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexService indexService) {
        this.statisticsService = statisticsService;
        this.indexService = indexService;
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
}
