package searchengine.services.sitehandler;

import lombok.Data;

@Data
public class Node {
    String domain;
    String nextUrlToParse;
}
