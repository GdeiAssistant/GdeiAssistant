package cn.gdeiassistant.common.config.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 默认使用 Jackson 3（tools.jackson.*）。
 * 现有代码里 Redis DAO / i18n 过滤器仍依赖 Jackson 2（com.fasterxml.jackson.*），
 * 这里补一个兼容 ObjectMapper，避免应用在启动阶段因为缺 bean 直接失败。
 */
@Configuration
public class LegacyJacksonConfig {

    @Bean
    public ObjectMapper legacyObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
