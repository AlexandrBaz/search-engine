package searchengine.services.repoServices;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Getter
@Scope("prototype")
public class ServiceStore {
    private SiteRepositoryService siteRepositoryService;
    private PageRepositoryService pageRepositoryService;
    private IndexRepositoryService indexRepositoryService;
    private LemmaRepositoryService lemmaRepositoryService;

    @Autowired
    public void setSiteRepositoryService(SiteRepositoryService siteRepositoryService){
        this.siteRepositoryService = siteRepositoryService;
    }

    @Autowired
    public void setPageRepositoryService(PageRepositoryService pageRepositoryService){
        this.pageRepositoryService = pageRepositoryService;
    }

    @Autowired
    public void setIndexRepositoryService(IndexRepositoryService indexRepositoryService){
        this.indexRepositoryService = indexRepositoryService;
    }

    @Autowired
    public void setLemmaRepositoryService(LemmaRepositoryService lemmaRepositoryService){
        this.lemmaRepositoryService = lemmaRepositoryService;
    }

}
