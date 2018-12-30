package com.linguancheng.gdeiassistant.Config;

import com.linguancheng.gdeiassistant.Converter.EncryptDataMappingJackson2HttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new EncryptDataMappingJackson2HttpMessageConverter());
        super.configureMessageConverters(converters);
    }
}
