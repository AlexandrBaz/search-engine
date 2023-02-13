package searchengine.services.sitehandler;

public class Node {
    private volatile String urlToCrawl;
    private volatile String domain;

    public Node(String urlToCrawl, String domain){
        this.urlToCrawl = urlToCrawl;
        this.domain = domain;
    }

    public String getUrlToCrawl() {
        return urlToCrawl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
