package edu.gdei.gdeiassistant.Config;

import edu.gdei.gdeiassistant.Filter.XSSFilter;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.nio.charset.StandardCharsets;

public class GdeiAssistantAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        //编码过滤器
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setForceEncoding(true);
        encodingFilter.setEncoding(StandardCharsets.UTF_8.displayName());
        FilterRegistration.Dynamic characterEncodingFilter = servletContext.addFilter("CharacterEncodingFilter", encodingFilter);
        //配置过滤器的过滤路径
        characterEncodingFilter.addMappingForUrlPatterns(null, true, "/*");

        //防止XSS攻击的表单信息过滤器
        FilterRegistration.Dynamic xssFilter = servletContext.addFilter("XSSFilter", new XSSFilter());
        xssFilter.addMappingForUrlPatterns(null, true, "/*");

        //配置Profile环境变量
        servletContext.setInitParameter("spring.profiles.default", "production");

        //设置超时时间为2小时
        servletContext.addListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                se.getSession().setMaxInactiveInterval(7200);
            }
        });

        //设置WebRootKey
        servletContext.setInitParameter("webAppRootKey", "GdeiAssistant");

        //配置Log4j路径
        servletContext.setInitParameter("log4jConfiguration", "classpath:config/log4j2/log4j2.xml");
        //添加Log4j监听器
        servletContext.addListener(Log4jServletContextListener.class);

        //配置DispatcherServlet
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(ApplicationContextConfig.class);
        ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("DispatcherServlet",
                new DispatcherServlet(applicationContext));
        //启动顺序
        dispatcherServlet.setLoadOnStartup(1);
        //支持异步
        dispatcherServlet.setAsyncSupported(true);
        //配置映射路径
        dispatcherServlet.addMapping("/");
    }
}
