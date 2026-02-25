package cn.gdeiassistant;

import cn.gdeiassistant.common.config.GraalRuntimeHints;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Spring Boot 启动类。
 * 可执行 jar/内嵌 Tomcat 运行。
 * 多数据源由 DataSource 配置类手动管理，排除自动配置避免多 Bean 冲突。
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
})
@ImportRuntimeHints(GraalRuntimeHints.class)
public class GdeiAssistantApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GdeiAssistantApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(GdeiAssistantApplication.class, args);
    }
}
