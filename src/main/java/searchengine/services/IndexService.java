package searchengine.services;

public interface IndexService {
    Boolean startIndexing();
    Boolean stopIndexing();
    Boolean indexPage(String url);

}
