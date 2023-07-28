package searchengine.utils.Parser;

import lombok.*;

import java.util.concurrent.Future;

@Data
public class Handler {
    String domain;
    Future<?> future;
    SiteRunnable worker;
}
