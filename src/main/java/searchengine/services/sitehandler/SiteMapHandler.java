package searchengine.services.sitehandler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveAction;

public class SiteMapHandler extends RecursiveAction{

    private final Node node;
    private static final Set<String> uniqueUrls = new TreeSet<>();
    private static int maxLevel;
    private static final Logger logger = LogManager.getLogger(SiteMapHandler.class);
    private final List<SiteMapHandler> tasks = new ArrayList<>();

    public SiteMapHandler(Node node) {
        this.node = node;
    }


    public List<SiteMapHandler> getNewUrlsForTasks() {
        try {
            Thread.sleep(300);
            String rootUrl = node.getUrlToCrawl();
            String domain = node.getDomain();
            Document document = Jsoup.connect(rootUrl).ignoreHttpErrors(true).get();
            Elements elements = document.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (childUrl.contains(domain) &&  childUrl.endsWith("/")) { //!isUniqueUrl(childUrl) &&
                    if (!childUrl.matches("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php)$") && !childUrl.contains("?") && !childUrl.contains("#")) {
                        synchronized (uniqueUrls) {
                            uniqueUrls.add(childUrl);
                        }
                        Node child = new Node(childUrl, domain);
                        SiteMapHandler siteMapCrawler = new SiteMapHandler(child);
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
