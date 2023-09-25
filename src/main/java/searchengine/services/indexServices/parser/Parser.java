package searchengine.services.indexServices.parser;

import lombok.*;

import java.util.concurrent.Future;

@Data
public class Parser {
    String domain;
    Future<?> future;
    SiteRunnable worker;
}
