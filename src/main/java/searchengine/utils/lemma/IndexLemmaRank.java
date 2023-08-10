package searchengine.utils.lemma;

public enum IndexLemmaRank {
    TITLE (1.2F),
    DESCRIPTION (1.1F),
    H1ELEMENTS (1.2F),
    H2ELEMENTS (1.1F),
    BODY (1.0F),
    FOOTER (0.8F);

    private final float multiplier;

    IndexLemmaRank(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getMultiplier() {
        return multiplier;
    }
}
