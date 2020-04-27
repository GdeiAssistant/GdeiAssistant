package edu.gdei.gdeiassistant.Config;

import ch.qos.logback.ext.spring.web.LogbackConfigListener;
import edu.gdei.gdeiassistant.Filter.XSSFilter;
import edu.gdei.gdeiassistant.Tools.StringUtils;
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

        //配置默认Profile环境变量
        servletContext.setInitParameter("spring.profiles.default", "development");

        //配置Logback配置文件路径和监听器
        String suffix = StringUtils.nullToBlank(System.getProperty("logback.profile.suffix"));
        servletContext.setInitParameter("logbackConfigLocation", "classpath:config/logback/logback" + suffix + ".xml");
        servletContext.addListener(LogbackConfigListener.class);

        //设置超时时间为2小时
        servletContext.addListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                se.getSession().setMaxInactiveInterval(7200);
            }
        });

        //设置WebRootKey
        servletContext.setInitParameter("webAppRootKey", "GdeiAssistant");

        //配置DispatcherServlet
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(ApplicationContextConfig.class);
        DispatcherServlet dispatcher = new DispatcherServlet(applicationContext);
        //发生404错误时抛出异常
        dispatcher.setThrowExceptionIfNoHandlerFound(true);
        ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("DispatcherServlet", dispatcher);
        //启动顺序
        dispatcherServlet.setLoadOnStartup(1);
        //支持异步
        dispatcherServlet.setAsyncSupported(true);
        //配置映射路径
        dispatcherServlet.addMapping("/");
    }
}
