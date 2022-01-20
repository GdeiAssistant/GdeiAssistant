package cn.gdeiassistant.Config.Application;

import cn.gdeiassistant.Constant.SettingConstantUtils;
import cn.gdeiassistant.Converter.EnumConvert.StringToLoginMethodEnumConverter;
import cn.gdeiassistant.Converter.EnumConvert.StringToQueryMethodEnumConverter;
import cn.gdeiassistant.Interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebMvc
public class ApplicationWebMvcConfig extends WebMvcConfigurerAdapter {

    /**
     * FreeMarker模板配置
     *
     * @return
     */
    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        freeMarkerConfigurer.setTemplateLoaderPath("/WEB-INF/view/");
        return freeMarkerConfigurer;
    }

    /**
     * FreeMarker视图渲染器
     *
     * @return
     */
    @Bean
    public FreeMarkerViewResolver freeMarkerViewResolver() {
        FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
        freeMarkerViewResolver.setContentType("text/html; charset=UTF-8");
        freeMarkerViewResolver.setExposeRequestAttributes(true);
        freeMarkerViewResolver.setExposeSessionAttributes(true);
        freeMarkerViewResolver.setExposeSpringMacroHelpers(true);
        freeMarkerViewResolver.setRedirectHttp10Compatible(false);
        freeMarkerViewResolver.setCache(true);
        freeMarkerViewResolver.setSuffix(".html");
        freeMarkerViewResolver.setOrder(0);
        return freeMarkerViewResolver;
    }

    /**
     * JSP视图渲染器
     *
     * @return
     */
    @Bean
    public InternalResourceViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
        internalResourceViewResolver.setPrefix("/WEB-INF/view/");
        internalResourceViewResolver.setSuffix(".jsp");
        internalResourceViewResolver.setRedirectHttp10Compatible(false);
        internalResourceViewResolver.setOrder(1);
        return internalResourceViewResolver;
    }

    /**
     * 登录拦截器不进行拦截的URL列表
     *
     * @return
     */
    @Bean
    public List<String> loginInterceptorExceptionList() {
        return Arrays.asList(SettingConstantUtils.LOGIN_INTERCEPTOR_EXCEPTION_LIST);
    }

    /**
     * 登录拦截器
     *
     * @return
     */
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor(loginInterceptorExceptionList());
    }

    /**
     * 添加拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor());
    }

    /**
     * 类型转换器
     *
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //字符串值转换为对应的枚举类值
        registry.addConverter(new StringToLoginMethodEnumConverter());
        registry.addConverter(new StringToQueryMethodEnumConverter());
    }

    /**
     * 将对于静态资源的请求转发到DefaultServlet进行处理
     *
     * 由于此项与DispatcherServlet的ThrowExceptionIfNoHandlerFound冲突，改为使用静态资源处理器来访问静态资源
     *
     * @param configurer
     */
    /*@Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }*/

    /**
     * 添加静态资源处理器
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
        registry.addResourceHandler("/img/**").addResourceLocations("/img/");
        registry.addResourceHandler("/mp3/**").addResourceLocations("/mp3/");
        registry.addResourceHandler("/font/**").addResourceLocations("/font/");
        registry.addResourceHandler("/txt/MP_verify_i9vujYHtkV4q7Kgx.txt").addResourceLocations("/MP_verify_i9vujYHtkV4q7Kgx.txt");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Map.Entry<String, String> entry : SettingConstantUtils.VIEW_CONTROLLER_NAME_MAP.entrySet()) {
            registry.addViewController(entry.getKey()).setViewName(entry.getValue());
        }
        super.addViewControllers(registry);
    }
}
