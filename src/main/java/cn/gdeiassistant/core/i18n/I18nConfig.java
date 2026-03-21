package cn.gdeiassistant.core.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class I18nConfig {

    @Value("${i18n.deepl.api-key:}")
    private String deeplApiKey;

    @Value("${i18n.deepl.api-url:https://api-free.deepl.com/v2/translate}")
    private String deeplApiUrl;

    @Value("${i18n.cache.ttl-days:7}")
    private int cacheTtlDays;

    public boolean isEnabled() {
        return deeplApiKey != null && !deeplApiKey.isBlank();
    }

    public String getDeeplApiKey() { return deeplApiKey; }
    public String getDeeplApiUrl() { return deeplApiUrl; }
    public int getCacheTtlDays() { return cacheTtlDays; }

    @Bean("i18nExecutor")
    @ConditionalOnProperty(name = "i18n.deepl.api-key")
    public Executor i18nExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("i18n-");
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnProperty(name = "i18n.deepl.api-key")
    public I18nTranslationService i18nTranslationService() {
        return new I18nTranslationService(this);
    }

    @Bean
    @ConditionalOnProperty(name = "i18n.deepl.api-key")
    public FilterRegistrationBean<I18nTranslationFilter> i18nFilter(
            I18nTranslationService translationService, ObjectMapper objectMapper) {
        FilterRegistrationBean<I18nTranslationFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new I18nTranslationFilter(translationService, objectMapper));
        bean.addUrlPatterns("/api/*");
        bean.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return bean;
    }
}
