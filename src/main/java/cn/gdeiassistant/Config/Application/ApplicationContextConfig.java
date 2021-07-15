package cn.gdeiassistant.Config.Application;

import cn.gdeiassistant.Converter.HttpMessageConvert.WeChatMappingJackson2HttpMessageConverter;
import cn.gdeiassistant.ErrorHandler.RestTemplateResponseErrorHandler;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;
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
     * AsyncRestTemplate
     *
     * @return
     */
    @Bean
    public AsyncRestTemplate asyncRestTemplate() {
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.getMessageConverters().add(new WeChatMappingJackson2HttpMessageConverter());
        asyncRestTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        return asyncRestTemplate;
    }

    /**
     * Properties配置文件读取器
     *
     * @return
     */
    @Bean
    public PropertiesFactoryBean propertiesReader() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setFileEncoding(StandardCharsets.UTF_8.displayName());
        propertiesFactoryBean.setLocations(ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
                .getResources("classpath*:/config/**/*.properties"));
        return propertiesFactoryBean;
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
     * 文件上传配置的MultipartResolver处理器
     *
     * @return
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        return commonsMultipartResolver;
    }
}
