package searchengine.dto.parser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jsoup.nodes.Document;

@Data
@EqualsAndHashCode
public class Page {
    String domain;
    String path;
    int pageStatusCode;
    Document document;
}
