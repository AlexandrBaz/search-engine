package searchengine.services.searchservices;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import searchengine.dto.search.SearchItem;
import searchengine.dto.search.SearchItemCached;
import searchengine.dto.search.SnippetRank;
import searchengine.model.PageEntity;
import searchengine.repositories.PageRepository;

import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SearchItemCreator {
    @Value(value = "${snippet.maxLength}")
    private int SNIPPET_MAX_LENGTH;
    @Value(value = "${snippet.tagsLength}")
    private int TAGS_LENGTH;
    @Value(value = "${snippet.wordsBeforeSearchQuery}")
    private int WORDS_BEFORE_QUERY_WORD;
    @Value(value = "${snippet.charsBeforeSearchQuery}")
    private int CHARS_BEFORE_QUERY_WORD;
    @Value(value = "${snippet.countWordsAfterCutting}")
    private int COUNT_WORDS_BEFORE_QUERY_WORD_AFTER_CUTTING;
    private final PageRepository pageRepository;

    @Autowired
    public SearchItemCreator(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public List<SearchItem> createSearchItem(@NotNull Page<SearchItemCached> searchItemCachedPage, String query) {
        List<SearchItem> searchItemList = new ArrayList<>();
        searchItemCachedPage.forEach(searchItemCached -> {
            PageEntity pageEntity = pageRepository.getReferenceById(searchItemCached.getPageId());
            SearchItem searchItem = new SearchItem();
            List<String> listWordsOfQuery = new ArrayList<>(Arrays.asList(query.split("\\s")));
            searchItem.setSite(pageEntity.getSite().getUrl().substring(0, pageEntity.getSite().getUrl().length() - 2));
            searchItem.setSiteName(pageEntity.getSite().getName());
            searchItem.setUri(pageEntity.getPath());
            searchItem.setTitle(getPageTitle(pageEntity.getContent()));
            searchItem.setSnippet(getSnippet(pageEntity.getContent(), listWordsOfQuery));
            searchItem.setRelevance(searchItemCached.getRelevance());
            searchItemList.add(searchItem);
        });
        return searchItemList;
    }

    private @NotNull String getPageTitle(String text) {
        Document document = Jsoup.parse(text);
        return document.getElementsByTag("title").text();
    }

    private @NotNull String getSnippet(String content, List<String> listWordsOfQuery) {
        ArrayList<String> bodySentences = getSentences(content);
        List<SnippetRank> snippetRankList = getSnippetMap(bodySentences, listWordsOfQuery);
        List<SnippetRank> sortedSnippets = snippetRankList.stream().parallel().sorted(Comparator.comparing(SnippetRank::getCount).reversed())
                .toList();
        StringBuilder stringBuilder = new StringBuilder();
        for (SnippetRank snippetRank : sortedSnippets) {
            int totalSnippetTagsLength = 0;
            totalSnippetTagsLength = totalSnippetTagsLength + (snippetRank.getCount() * TAGS_LENGTH);
            int snippedLength = snippetRank.getSnippet().length() - totalSnippetTagsLength;
            if (snippedLength <= SNIPPET_MAX_LENGTH) {
                stringBuilder.append(snippetRank.getSnippet()).append(" ");
            } else {
                int wordsBeforeWordQuery = getNumWordsBeforeQuery(snippetRank.getSnippet());
                if (wordsBeforeWordQuery > WORDS_BEFORE_QUERY_WORD) {
                    stringBuilder.append(getShortSnippet(wordsBeforeWordQuery, snippetRank.getSnippet()));
                } else {
                    stringBuilder.append(snippetRank.getSnippet()).append(" ");
                }
            }
            int checkingLength = stringBuilder.length() - totalSnippetTagsLength;
            if (checkingLength >= SNIPPET_MAX_LENGTH) {
                return stringBuilder.substring(0, SNIPPET_MAX_LENGTH + totalSnippetTagsLength).concat("...");
            }
        }
        return sortedSnippets.get(0).getSnippet();
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
        int count = 0;
        StringTokenizer st = new StringTokenizer(sentence, " \t\n\r,.?!\"«»", true);
        while (st.hasMoreTokens()) {
            String tokenWord = st.nextToken();
            String tokenWordToLowercase = tokenWord.toLowerCase();
            if (tokenWordToLowercase.equals(searchWord)) {
                count++;
                stringBuilder.append("<b>").append(tokenWord).append("</b>");
            } else {
                stringBuilder.append(tokenWord);
            }
        }
        snippetRank.setSnippet(stringBuilder.toString().trim());
        snippetRank.setCount(count);
        return snippetRank;
    }

    private int getNumWordsBeforeQuery(@NotNull String snippet) {
        AtomicInteger wordsBefore = new AtomicInteger();
        String[] snippetToWords = snippet.split("\\s");
        for (String word : snippetToWords) {
            if (!word.contains("<b")) {
                wordsBefore.getAndAdd(1);
            } else {
                break;
            }
        }
        return wordsBefore.get();
    }

    private @NotNull String getShortSnippet(int wordsBefore, @NotNull String snippet) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] snippetToWords = snippet.split("\\s");
        int start = 0;
        for (String word : snippetToWords) {
            stringBuilder.append(word).append(" ");
            start++;
            if (start == wordsBefore) {
                break;
            }
        }
        StringBuilder str = new StringBuilder();
        if (stringBuilder.toString().length() > CHARS_BEFORE_QUERY_WORD) {
            int startShortSnippet = wordsBefore - COUNT_WORDS_BEFORE_QUERY_WORD_AFTER_CUTTING;
            int snippetStart = 0;
            for (String word : snippetToWords) {
                if (snippetStart >= startShortSnippet) {
                    str.append(word).append(" ");
                }
                snippetStart++;
            }
        }
        return str.toString();
    }
}