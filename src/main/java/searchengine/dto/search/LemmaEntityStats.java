package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.LemmaEntity;

@Data
@AllArgsConstructor
public class LemmaEntityStats {
    private LemmaEntity lemmaEntity;
    private float percent;
}
