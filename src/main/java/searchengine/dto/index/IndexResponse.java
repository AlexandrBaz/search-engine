package searchengine.dto.index;

import lombok.Data;
import searchengine.dto.statistics.StatisticsData;

@Data
public class IndexResponse {
    private boolean result;
    private StatisticsData statistics;
}
