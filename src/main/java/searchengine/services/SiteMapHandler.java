package searchengine.services;

import java.io.IOException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public class SiteMapHandler extends RecursiveAction {
    private final String domain;

    public SiteMapHandler(String domain) {
        this.domain = domain;
    }

    public List<SiteMapHandler> getNewUrlsForTasks() {
        try {
            Thread.sleep(300);
            String rootUrl = node.getUrlToCrawl();
            String domain = node.getDomain();
            Document document = Jsoup.connect(rootUrl).ignoreHttpErrors(true).get();
            Elements elements = document.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (childUrl.contains(domain) && !isUniqueUrl(childUrl) && childUrl.endsWith("/")) {
                    if (!childUrl.matches("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php)$") && !childUrl.contains("?") && !childUrl.contains("#")) {
                        synchronized (uniqueUrls) {
                            uniqueUrls.add(childUrl);
                            if (maxLevel < getLevel(childUrl)){
                                setMaxLevel(getLevel(childUrl));
                            }
                        }
                        Node child = new Node(childUrl, domain);
                        SiteMapCrawler siteMapCrawler = new SiteMapCrawler(child);
                        siteMapCrawler.fork();
                        tasks.add(siteMapCrawler);
                        System.out.println("in progress");
                    }
                }
            });
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
        return tasks;
    }

    @Override
    protected void compute() {
        List<SiteMapHandler> allTasks = getNewUrlsForTasks();
        for (SiteMapHandler task : allTasks){
            task.join();
        }
    }
}
