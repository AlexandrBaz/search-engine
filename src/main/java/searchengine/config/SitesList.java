package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;
//    SitesList(){
//        sites.add(new Site("https://www.lenta.ru","Лента.ру"));
//        sites.add(new Site("https://www.skillbox.ru","Skillbox"));
//        sites.add(new Site("https://www.playback.ru","PlayBack.Ru"));
//    }
}
