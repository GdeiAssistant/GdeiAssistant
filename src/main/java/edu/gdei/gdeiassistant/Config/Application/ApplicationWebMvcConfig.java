package edu.gdei.gdeiassistant.Config.Application;

import edu.gdei.gdeiassistant.Converter.EnumConvert.StringToAuthenticationTypeEnumConverter;
import edu.gdei.gdeiassistant.Converter.EnumConvert.StringToLoginMethodEnumConverter;
import edu.gdei.gdeiassistant.Converter.EnumConvert.StringToQueryMethodEnumConverter;
import edu.gdei.gdeiassistant.Interceptor.AuthenticationInterceptor;
import edu.gdei.gdeiassistant.Interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
        List<String> loginInterceptorExceptionList = new ArrayList<>();
        //退出账号
        loginInterceptorExceptionList.add("/logout");
        //登录账号
        loginInterceptorExceptionList.add("/login");
        //Cron作业
        loginInterceptorExceptionList.add("/cron");
        //用户登录接口
        loginInterceptorExceptionList.add("/api/userlogin");
        //下载接口
        loginInterceptorExceptionList.add("/download");
        //协议与政策
        loginInterceptorExceptionList.add("/agreement");
        loginInterceptorExceptionList.add("/policy");
        loginInterceptorExceptionList.add("/license");
        //公告声明
        loginInterceptorExceptionList.add("/announcement");
        //软件说明
        loginInterceptorExceptionList.add("/about");
        //Restful API
        loginInterceptorExceptionList.add("/rest");
        //微信API接口
        loginInterceptorExceptionList.add("/wechat");
        //QQAPI接口
        loginInterceptorExceptionList.add("/qq");
        //易班API接口
        loginInterceptorExceptionList.add("/yiban");
        //支付宝API接口
        loginInterceptorExceptionList.add("/alipay");
        return loginInterceptorExceptionList;
    }

    @Bean
    public List<String> authenticationInterceptorExceptionList() {
        List<String> authenticationInterceptorExceptionList = new ArrayList<>();
        //退出账号
        authenticationInterceptorExceptionList.add("/logout");
        //登录账号
        authenticationInterceptorExceptionList.add("/login");
        //Cron作业
        authenticationInterceptorExceptionList.add("/cron");
        //用户登录接口
        authenticationInterceptorExceptionList.add("/api/userlogin");
        authenticationInterceptorExceptionList.add("/rest/userlogin");
        //权限令牌操作
        authenticationInterceptorExceptionList.add("/rest/token");
        //实名认证页面和接口
        authenticationInterceptorExceptionList.add("/authentication");
        authenticationInterceptorExceptionList.add("/api/authentication");
        //注销页面和接口
        authenticationInterceptorExceptionList.add("/close");
        authenticationInterceptorExceptionList.add("/api/close");
        //下载接口
        authenticationInterceptorExceptionList.add("/download");
        //协议与政策
        authenticationInterceptorExceptionList.add("/agreement");
        authenticationInterceptorExceptionList.add("/policy");
        authenticationInterceptorExceptionList.add("/license");
        //公告声明
        authenticationInterceptorExceptionList.add("/announcement");
        //软件说明
        authenticationInterceptorExceptionList.add("/about");
        //微信API接口
        authenticationInterceptorExceptionList.add("/wechat");
        //QQAPI接口
        authenticationInterceptorExceptionList.add("/qq");
        //易班API接口
        authenticationInterceptorExceptionList.add("/yiban");
        //支付宝API接口
        authenticationInterceptorExceptionList.add("/alipay");
        return authenticationInterceptorExceptionList;
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

    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor(authenticationInterceptorExceptionList());
    }

    /**
     * 添加拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor());
        registry.addInterceptor(authenticationInterceptor());
    }

    /**
     * 类型转换器
     *
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //字符串值转换为对应的枚举类值
        registry.addConverter(new StringToAuthenticationTypeEnumConverter());
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
        registry.addViewController("/").setViewName("About/index");
        registry.addViewController("/about").setViewName("About/index");
        registry.addViewController("/about/security").setViewName("About/security");
        registry.addViewController("/about/account").setViewName("About/account");
        registry.addViewController("/about/wechat").setViewName("About/wechat");
        registry.addViewController("/license").setViewName("About/license");
        registry.addViewController("/agreement").setViewName("AgreementAndPolicy/agreement");
        registry.addViewController("/policy/cookie").setViewName("AgreementAndPolicy/cookiePolicy");
        registry.addViewController("/policy/privacy").setViewName("AgreementAndPolicy/privacyPolicy");
        registry.addViewController("/policy/social").setViewName("AgreementAndPolicy/socialPolicy");
        registry.addViewController("/about/graduation").setViewName("AgreementAndPolicy/graduationPolicy");
        registry.addViewController("/announcement/equalrights").setViewName("Announcement/equalrights");
        registry.addViewController("/announcement/suicideprevention").setViewName("Announcement/suicideprevention");
        super.addViewControllers(registry);
    }
}
