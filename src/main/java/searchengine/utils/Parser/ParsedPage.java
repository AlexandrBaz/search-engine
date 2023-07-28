package searchengine.utils.Parser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@NoArgsConstructor
@Scope("prototype")
public class ParsedPage {
    String domain;
    String path;
    String urlToParse;
    int pageStatusCode;
    Document document;
}
