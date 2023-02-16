package searchengine.services.sitehandler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.IndexRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveAction;

public class SiteMapHandler extends RecursiveAction {

    IndexRepository indexRepository;
    private final Node node;
    private static final Set<String> uniqueUrls = new TreeSet<>();
    private static final Logger logger = LogManager.getLogger(SiteMapHandler.class);
    private final List<SiteMapHandler> tasks = new ArrayList<>();

    private final String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[#?]";

    public SiteMapHandler(Node node, IndexRepository indexRepository) {
        this.node = node;
        this.indexRepository = indexRepository;
    }

    @Override
    protected void compute() {
        List<SiteMapHandler> allTasks = getNewUrlsForTasks();
        invokeAll(allTasks);
    }

    public void ParsePage(String url) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getUrlStatus(response);
    }

    public int getUrlStatus(Connection.Response response) {
        return response.statusCode();
    }

    public List<String> getOnPageUrls(Connection.Response response) {
        String domain = node.getDomain();
        List<String> onPageUrls = new ArrayList<>();
        try {
            Thread.sleep(300);
            Document doc = response.parse();
            Elements elements = doc.select("a[href]");
            elements.stream().map((link) -> link.attr("abs:href")).forEachOrdered((childUrl) -> {
                if (childUrl.contains(domain) && childUrl.endsWith("/")) { //!isUniqueUrl(childUrl) &&
                    if (urlNoMedia(childUrl)) {
                        synchronized (uniqueUrls) {
                            uniqueUrls.add(childUrl);
                        }
                        Node child = new Node(childUrl, domain);
                        SiteMapHandler siteMapCrawler = new SiteMapHandler(child, indexRepository);
                        siteMapCrawler.fork();
                        tasks.add(siteMapCrawler);
                        System.out.println("in progress");
                    }
                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error(e);
        }
        return onPageUrls;
    }

    public boolean urlNoMedia(String url){
        return !url.matches(mediaRegex) && url.contains(node.getDomain());
    }

    public boolean urlIsValid(String url){

    }

}
