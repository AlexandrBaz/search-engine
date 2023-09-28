package searchengine.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.yml")
@Getter
public class AppConfig {
    @Value("${batch.parse}")
    private int parseBatchSize;
    @Value("${batch.index}")
    private int indexSliceSize;
    @Value("${batch.indexWrite}")
    private int indexWriteSize;
    @Value("${batch.lemma}")
    private int lemmaSliceSize;
    @Value("${url.mediaRegex}")
    private String mediaRegex;
    @Value("${url.ignoreStatusCode}")
    private String code4xx5xx;
    @Value("${jsoup-setting.useragent}")
    private String UserAgent;
    @Value("${jsoup-setting.timeout}")
    private int timeOut;
    @Value("${jsoup-setting.ignoreHttpErrors}")
    private boolean ignoreHttpErrors;
    @Value("${jsoup-setting.sleep}")
    private int threadSleep;
    @Value("${lemma.wordType}")
    private String wordType;
    @Value("${lemma.particlesNames}")
    private String[] particlesNames;
}
