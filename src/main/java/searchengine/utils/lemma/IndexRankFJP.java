package searchengine.utils.lemma;

import lombok.extern.log4j.Log4j2;
import searchengine.model.PageEntity;
import searchengine.utils.parser.SiteParserBatch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

@Log4j2
public class IndexRankFJP extends RecursiveAction {
    @Override
    protected void compute() {
        if (parseActive()) {
            CopyOnWriteArrayList<IndexRankFJP> indexRankFJPList = getTasksAndFork();
            for (IndexRankFJP task : indexRankFJPList) {
                task.join();
            }
        } else {
            log.info(Thread.currentThread().getName() + " " + Thread.currentThread().isInterrupted() + " stop");
        }
    }

    private List<IndexRankFJP> getTasksAndFork() {
        List<PageEntity> pageEntityList;
        pageEntityList.f


        slice.getContent().parallelStream().unordered()
                .forEach(pageEntity -> {
                    Map<String, Float> lemmaRankFromPage = lemmaFinder.getAllIndexRankOfPage(pageEntity);
                    collectIndexLemma(lemmaRankFromPage, pageEntity, siteEntity);
                    lemmaRankFromPage.clear();
                });
        return ;
    }
}
