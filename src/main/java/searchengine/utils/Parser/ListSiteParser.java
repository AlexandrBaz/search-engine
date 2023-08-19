package searchengine.utils.Parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.parser.Page;
import searchengine.services.PageRepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;


public class ListSiteParser extends RecursiveAction {
    private final List<String> listUrlsToParse;
    private final String domain;
    private final CopyOnWriteArrayList<Page> pageEntityList;
    private final PageRepositoryService pageRepositoryService;
    private final SiteRunnable siteRunnable;

    private final ArrayList<String> uniqUrlList;

    public ListSiteParser(List<String> listUrlsToParse, String domain, CopyOnWriteArrayList<Page> pageEntityList, ArrayList<String> uniqUrlList, SiteRunnable siteRunnable) {
        this.listUrlsToParse = listUrlsToParse;
        this.domain = domain;
        this.pageEntityList = pageEntityList;
        this.uniqUrlList = uniqUrlList;
        this.pageRepositoryService = siteRunnable.getPageRepositoryService();
        this.siteRunnable = siteRunnable;
    }

    @Override
    protected void compute() {
        List<ListSiteParser> listSiteParsers = getTasksAndFork();
//        invokeAll(listSiteParsers);
        for (ListSiteParser task : listSiteParsers) {
            task.join();
        }
    }

    public synchronized List<ListSiteParser> getTasksAndFork() {
        System.out.println(Thread.currentThread().getName() + " task start");
        List<ListSiteParser> tasks = new ArrayList<>();
        listUrlsToParse.forEach(url -> {
            if (urlIsValid(url) && urlIsUnique(url)) {
                    tasks.add(getChild(url));
                    System.out.println(uniqUrlList.size() + " uniqUrlList");
                }
        });
        System.out.println(Thread.currentThread().getName() + " task send");
        return tasks;
    }

    private synchronized boolean urlIsUnique(String url) {
        boolean uniq = true;
        if (!uniqUrlList.isEmpty()) {
            for (String uniqUrl : uniqUrlList) {
                if (uniqUrl.equals(url)) {
                    uniq = false;
                    break;
                }
            }
        }
        if (uniq){
            uniqUrlList.add(url);
        }
        return uniq;
    }

    private synchronized boolean urlIsValid(@NotNull String url) {
        String mediaRegex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|pdf|php|zip)$|[?|#]";
        return !url.matches(mediaRegex)
                && url.startsWith(domain)
                && (url.endsWith("/") || url.endsWith("html"));
    }

    private synchronized ListSiteParser getChild(String url) {
        List<String> listChildUrlsToParse = getListUrlsToParse(url);
        ListSiteParser listSiteParser = new ListSiteParser(listChildUrlsToParse, domain, pageEntityList, uniqUrlList, siteRunnable);
        listSiteParser.fork();
        return listSiteParser;
    }

    private synchronized List<String> getListUrlsToParse(String url) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return parseUrl(url);
    }

    private synchronized List<String> parseUrl(String url) {
        List<String> urlList;
            try {
                Connection.Response response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .timeout(20000)
                        .ignoreHttpErrors(true)
                        .execute();
                Document document = response.parse();
                createPage(document, url, response.statusCode());
                Elements elements = document.select("a[href]");
                urlList = elements.stream().map(absUrl -> absUrl.attr("abs:href")).toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        return urlList;
    }

    private synchronized void createPage(Document document, @NotNull String url, int statusCode) {
        Page page = new Page();
        page.setDocument(document);
        page.setPath(url.replaceAll(domain, "/"));
        page.setPageStatusCode(statusCode);
        page.setDomain(domain);
        pageEntityList.add(page);
        checkAndWriteToDB(pageEntityList);
    }

    private synchronized void checkAndWriteToDB(@NotNull List<Page> pageList) {
        if (pageList.size() == 100) {
            pageRepositoryService.addListPageEntity(pageEntityList, domain);
            pageEntityList.clear();
        }
    }
}
