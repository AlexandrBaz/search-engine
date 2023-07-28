package searchengine.dto.parser;

import lombok.Data;
import org.jsoup.nodes.Document;

@Data
public class Page {
    String domain;
    String path;
    int pageStatusCode;
    Document document;
}
