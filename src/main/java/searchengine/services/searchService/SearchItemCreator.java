package searchengine.services.searchService;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.dto.search.SearchItem;
import searchengine.dto.search.SnippetRank;
import searchengine.model.PageEntity;

import java.text.BreakIterator;
import java.util.*;

public class SearchItemCreator {

    public SearchItem createSearchItem(@NotNull PageEntity pageEntity, @NotNull SearchItem searchItem, String query) {
        List<String> listWordsOfQuery = getListWordsOfQuery(query);
        searchItem.setSiteName(pageEntity.getSite().getName());
        searchItem.setSite(pageEntity.getSite().getUrl().substring(0, pageEntity.getSite().getUrl().length()-2));
        searchItem.setUri(pageEntity.getPath());
        searchItem.setTitle(getPageTitle(pageEntity.getContent()));
        searchItem.setSnippet(getSnippet(pageEntity.getContent(), listWordsOfQuery));
        return searchItem;
    }

    private @NotNull String getPageTitle(String text) {
        Document document = Jsoup.parse(text);
        return document.getElementsByTag("title").text();
    }

    private @NotNull String getSnippet(String content, List<String> listWordsOfQuery) {
        ArrayList<String> bodySentences = getSentences(content);
        List<SnippetRank> snippetRankList = getSnippetMap(bodySentences, listWordsOfQuery);
//        snippetRankList.forEach(snippetRank -> System.out.println(snippetRank.getCount() + " -> " + snippetRank.getSnippet()));
        List<SnippetRank> sortedSnippets = snippetRankList.stream().parallel().sorted(Comparator.comparing(SnippetRank::getCount).reversed())
                .toList();
        return sortedSnippets.get(0).getSnippet();
    }

    private List<SnippetRank> getSnippetMap(@NotNull ArrayList<String> bodySentences, List<String> listWordsOfQuery) {
        return bodySentences.stream().parallel()
                .map(sentence -> sentenceMarkedUp(sentence, listWordsOfQuery))
                .sorted(Comparator.comparing(SnippetRank::getCount).reversed())
                .toList();
    }

    private @NotNull SnippetRank sentenceMarkedUp(String sentence, @NotNull List<String> listWordsOfQuery) {
        SnippetRank finalSnippetRank = new SnippetRank();
        finalSnippetRank.setSnippet(sentence);
        for (String searchWord : listWordsOfQuery) {
            SnippetRank snippetRank = markSnippet(finalSnippetRank.getSnippet(), searchWord);
            finalSnippetRank.setSnippet(snippetRank.getSnippet());
            finalSnippetRank.setCount(finalSnippetRank.getCount() + snippetRank.getCount());
        }
        return finalSnippetRank;
    }

    private @NotNull SnippetRank markSnippet(@NotNull String sentence, @NotNull String searchWord) {
        SnippetRank snippetRank = new SnippetRank();
        StringBuilder stringBuilder = new StringBuilder();
        String firstChar = searchWord.substring(0, 1);
        String searchFirstUpper = searchWord.replaceFirst(firstChar, firstChar.toUpperCase());
        String searchUppercase = searchWord.toUpperCase();
        int count = 0;
        StringTokenizer st = new StringTokenizer(sentence, " \t\n\r,.?!\"«»", true);
        while (st.hasMoreTokens()) {
            String tokenWord = st.nextToken();
            String tokenWordToLowercase = tokenWord.toLowerCase();
            if(tokenWordToLowercase.equals(searchWord)){
                count = tokenWord.length()>2 ? count+1 : count;
                stringBuilder.append("<b>").append(tokenWord).append("</b>");
//            }
//            if (tokenWord.equals(searchWord)) {
//                count = tokenWord.length()>2 ? count+1 : count;
//                stringBuilder.append("<b>").append(searchWord).append("</b>");
//            } else if (tokenWord.equals(searchFirstUpper)) {
//                count = tokenWord.length()>2 ? count+1 : count;
//                stringBuilder.append("<b>").append(searchFirstUpper).append("</b>");
//            } else if (tokenWord.equals(searchUppercase)) {
//                count = tokenWord.length()>2 ? count+1 : count;
//                stringBuilder.append("<b>").append(searchUppercase).append("</b>");
            } else {
                stringBuilder.append(tokenWord);
            }
        }
        snippetRank.setSnippet(stringBuilder.toString().trim());
        snippetRank.setCount(count);
        return snippetRank;
    }

    private @NotNull ArrayList<String> getSentences(String content) {
        Document document = Jsoup.parse(content);
        String body = document.getElementsByTag("body").text();
        ArrayList<String> bodySentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(new Locale("ru", "RU"));
        iterator.setText(body);
        int start = iterator.first();
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            bodySentences.add(body.substring(start, end));
        }
        return bodySentences;
    }

    @Contract("_ -> new")
    private @NotNull List<String> getListWordsOfQuery(@NotNull String query) {
        return new ArrayList<>(Arrays.asList(query.split("\\s")));
    }
}
