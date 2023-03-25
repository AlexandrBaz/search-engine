package searchengine.services.sitehandler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SiteToCrawl {
    String domain;
    String urlToCrawl;
}
