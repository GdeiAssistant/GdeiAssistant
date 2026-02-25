package cn.gdeiassistant.common.config.Application;

import cn.gdeiassistant.common.converter.HttpMessageConvert.WeChatMappingJackson2HttpMessageConverter;
import cn.gdeiassistant.common.errorhandler.RestTemplateResponseErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@Configuration
@ComponentScan(basePackages = "cn.gdeiassistant")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class ApplicationContextConfig {

    /**
     * RestTemplate
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new WeChatMappingJackson2HttpMessageConverter());
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        return restTemplate;
    }

    /**
     * 注册方法级别验证后处理器
     *
     * @return
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * 国际化支持
     *
     * @return
     */
    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        resourceBundleMessageSource.setBasename("i18n/messages");
        return resourceBundleMessageSource;
    }
}
