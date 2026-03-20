package cn.gdeiassistant.common.config.Application;

import cn.gdeiassistant.common.constant.SettingConstantUtils;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * 前后端分离：后端仅提供 REST API，鉴权依赖 JwtSessionIdFilter 从 Authorization: Bearer &lt;JWT&gt; 读取，
 * 不依赖 Cookie/Session。
 */

@Configuration
@EnableWebMvc
public class ApplicationWebMvcConfig implements WebMvcConfigurer {

    private static final String[] DEFAULT_ALLOWED_ORIGIN_PATTERNS = {
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:4173",
            "http://127.0.0.1:4173"
    };

    @Value("${app.cors.allowed-origin-patterns:}")
    private String allowedOriginPatterns;

    /**
     * API 鉴权放行的 URL 前缀或路径（无状态，不依赖 Session）
     */
    @Bean
    public List<String> apiAuthExceptionList() {
        return Arrays.asList(SettingConstantUtils.LOGIN_INTERCEPTOR_EXCEPTION_LIST);
    }

    @Bean
    public ApiAuthInterceptor apiAuthInterceptor() {
        return new ApiAuthInterceptor(apiAuthExceptionList());
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor());
        registry.addInterceptor(apiAuthInterceptor());
    }

    /**
     * CORS：允许前端跨域请求时携带 Authorization 等头，避免 401。
     * 开发时若用 Vite 代理则同源无 CORS；生产或直连后端时需此配置。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(resolveAllowedOriginPatterns())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Authorization", "X-Client-Type", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }

    private String[] resolveAllowedOriginPatterns() {
        if (allowedOriginPatterns == null || allowedOriginPatterns.trim().isEmpty()) {
            return DEFAULT_ALLOWED_ORIGIN_PATTERNS;
        }
        String[] patterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toArray(String[]::new);
        return patterns.length == 0 ? DEFAULT_ALLOWED_ORIGIN_PATTERNS : patterns;
    }

    /**
     * 类型转换器
     *
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //字符串值转换为对应的枚举类值
    }

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
        registry.addResourceHandler("/doc/**").addResourceLocations("/doc/");
        registry.addResourceHandler("/mp3/**").addResourceLocations("/mp3/");
        registry.addResourceHandler("/font/**").addResourceLocations("/font/");
        // 前后端分离后，微信验证文件由前端 Nginx 负责代理，后端不再提供
        // registry.addResourceHandler("/txt/MP_verify_i9vujYHtkV4q7Kgx.txt").addResourceLocations("/MP_verify_i9vujYHtkV4q7Kgx.txt");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // JSP 已移除，不再注册视图控制器
    }
}
