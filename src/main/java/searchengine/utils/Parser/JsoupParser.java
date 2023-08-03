package searchengine.utils.Parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.properties.ConfigurationProperties;
import searchengine.dto.index.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JsoupParser {
    Connection.Response response = null;

    public synchronized Elements getUrlsToPears(String url){
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        response = getResponse(url);
        return getUrls(response);
    }

    private Connection.Response getResponse(String url){
        try {
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(10000)
                    .execute();
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return response;
    }

    public int getSitemapStatus() {
        return response.statusCode();
    }

    private Elements getUrls(Connection.Response response) {
        ArrayList<String> urls = new ArrayList<String>();
        Document doc = null;
        try {
            doc = response.parse();
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return doc.select("a[href]");

    }
}