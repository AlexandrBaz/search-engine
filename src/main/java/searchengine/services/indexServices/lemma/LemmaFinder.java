package searchengine.services.indexServices.lemma;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LemmaFinder {
    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^А-Яа-яёЁ\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС", "МС-П", "ВВОДН"};
    private final static String code4xx5xx = "[45]\\d{2}";

    public static @NotNull LemmaFinder getInstance() throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        return new LemmaFinder(morphology);
    }

    private LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    private LemmaFinder() {
        throw new RuntimeException("Disallow construct");
    }

    public ConcurrentHashMap<String, Integer> collectLemmas(String text) {
        ConcurrentHashMap<String, Integer> lemmas = new ConcurrentHashMap<>();
        partitionDocument(text).forEach((key, value) -> {
            if (!value.isBlank()) {
                String[] words = arrayContainsRussianWords(value);
                checkWordsAndPutToMap(words, lemmas);
            }
        });

        return lemmas;
    }

    private void checkWordsAndPutToMap(String @NotNull [] words, ConcurrentHashMap<String, Integer> lemmas) {
        for (String word : words) {
            if (!word.isBlank() && isCorrectWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                List<String> normalForms = getNormalWords(wordBaseForms, word);
                putToLemmaMap(normalForms, lemmas);
            }
        }
    }

    private List<String> getNormalWords(List<String> wordBaseForms, String word) {
        List<String> normalForms = new ArrayList<>();
        if (!anyWordBaseBelongToParticle(wordBaseForms)) {
            normalForms = luceneMorphology.getNormalForms(word);
        }
        return normalForms;
    }

    private void putToLemmaMap(@NotNull List<String> normalForms, ConcurrentHashMap<String, Integer> lemmas) {
        if (!normalForms.isEmpty()) {
            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    public ConcurrentHashMap<String, String> partitionDocument(String page) {
        Document document = Jsoup.parse(page);
        Elements title = document.getElementsByTag("title").remove();
        Elements description = document.getElementsByTag("description").remove();
        Elements footer = document.getElementsByTag("footer").remove();
        Elements h1Elements = document.getElementsByTag("h1").remove();
        Elements h2Elements = document.getElementsByTag("h2").remove();
        Elements body = document.getElementsByTag("body").remove();
        ConcurrentHashMap<String, String> pageParts = new ConcurrentHashMap<>();
        pageParts.put("title", title.text());
        pageParts.put("description", description.text());
        pageParts.put("h1Elements", h1Elements.text());
        pageParts.put("h2Elements", h2Elements.text());
        pageParts.put("body", body.text());
        pageParts.put("footer", footer.text());
        return pageParts;
    }

    private boolean anyWordBaseBelongToParticle(@NotNull List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String @NotNull [] arrayContainsRussianWords(@NotNull String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[ёЁ]", "е")
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    public @NotNull ConcurrentHashMap<String, Float> getAllIndexRankOfPage(@NotNull PageEntity pageEntity) {
        ConcurrentHashMap<String, Float> pageIndexMap = new ConcurrentHashMap<>();
        if (!pageEntity.getCode().toString().equals(code4xx5xx)) {
            partitionDocument(pageEntity.getContent()).forEach((partName, text) -> {
                ConcurrentHashMap<String, Float> lemmaFromPagePart = switch (partName) {
                    case ("title") -> getLemmaFromPagePart(text, IndexLemmaRank.TITLE.getMultiplier());
                    case ("description") -> getLemmaFromPagePart(text, IndexLemmaRank.DESCRIPTION.getMultiplier());
                    case ("h1Elements") -> getLemmaFromPagePart(text, IndexLemmaRank.H1ELEMENTS.getMultiplier());
                    case ("h2Elements") -> getLemmaFromPagePart(text, IndexLemmaRank.H2ELEMENTS.getMultiplier());
                    case ("footer") -> getLemmaFromPagePart(text, IndexLemmaRank.FOOTER.getMultiplier());
                    default -> getLemmaFromPagePart(text, IndexLemmaRank.BODY.getMultiplier());
                };
                lemmaFromPagePart.forEach((lemma, rank) -> {
                    if (pageIndexMap.containsKey(lemma)) {
                        pageIndexMap.computeIfPresent(lemma, (key, value) -> value + rank);
                    } else {
                        pageIndexMap.put(lemma, rank);
                    }
                });
            });
        }
        return pageIndexMap;
    }

    private @NotNull ConcurrentHashMap<String, Float> getLemmaFromPagePart(String partOfPage, Float lemmaMultiplier) {
        Map<String, Integer> temporaryMap = collectLemmas(partOfPage);
        ConcurrentHashMap<String, Float> finalMap = new ConcurrentHashMap<>();
        temporaryMap.forEach((key, value) -> finalMap.put(key, value * lemmaMultiplier));
        return finalMap;
    }
}