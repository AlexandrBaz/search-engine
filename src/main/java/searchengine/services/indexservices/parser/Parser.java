package searchengine.services.indexservices.parser;

import lombok.*;

import java.util.concurrent.Future;

@Data
public class Parser {
    String domain;
    Future<?> future;
    SiteRunnable worker;
}
