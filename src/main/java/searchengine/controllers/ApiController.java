package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.index.TrueResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StartIndexingService;
import searchengine.services.StatisticsService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;

    @Autowired
    public ApiController(StatisticsService statisticsService, StartIndexingService startIndexingService) {
        this.statisticsService = statisticsService;
        this.startIndexingService = startIndexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing(){
        return ResponseEntity.ok(startIndexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Boolean> stopIndexing(){
        return ResponseEntity.ok(startIndexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Boolean> indexPage(){
        return ResponseEntity.ok(startIndexingService.indexPage());
    }
}
