package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import searchengine.model.LemmaEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LemmaEntityRank {
    LemmaEntity lemmaEntity;
    float percent;
}
