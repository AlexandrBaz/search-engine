package searchengine.services.sitehandler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;


public class JsoupParser {

    @ConfigurationProperties(prefix = "jsoup-setting")
    public ParsedPage getDocument(ParsedPage parsedPage) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            error.printStackTrace();
            System.out.println("isInterrupted " + Thread.currentThread().isInterrupted());
            System.out.println("Alive " + Thread.currentThread().isAlive());
//            setParseError(getSiteFromTable(), error.toString());
//            logger.error(e);
        }
        Connection.Response response;
        Document document;
        try {
            response = Jsoup.connect(parsedPage.getUrlToParse()).execute();
            document = response.parse();
            parsedPage.setDocument(document);
            parsedPage.setPageStatusCode(response.statusCode());
            parsedPage.setPath(parsedPage.getUrlToParse().replaceAll(parsedPage.getDomain(), "/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return parsedPage;
    }
}