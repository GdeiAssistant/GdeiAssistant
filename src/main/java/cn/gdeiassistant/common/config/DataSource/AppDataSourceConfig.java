package cn.gdeiassistant.common.config.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@MapperScan(basePackages = {
        "cn.gdeiassistant.core.campuscredential.mapper",
        "cn.gdeiassistant.core.authentication.mapper",
        "cn.gdeiassistant.core.cet.mapper",
        "cn.gdeiassistant.core.cetquery.mapper",
        "cn.gdeiassistant.core.cron.mapper",
        "cn.gdeiassistant.core.data.mapper",
        "cn.gdeiassistant.core.dating.mapper",
        "cn.gdeiassistant.core.delivery.mapper",
        "cn.gdeiassistant.core.email.mapper",
        "cn.gdeiassistant.core.message.mapper",
        "cn.gdeiassistant.core.marketplace.mapper",
        "cn.gdeiassistant.core.express.mapper",
        "cn.gdeiassistant.core.feedback.mapper",
        "cn.gdeiassistant.core.lostandfound.mapper",
        "cn.gdeiassistant.core.phone.mapper",
        "cn.gdeiassistant.core.photograph.mapper",
        "cn.gdeiassistant.core.privacy.mapper",
        "cn.gdeiassistant.core.profile.mapper",
        "cn.gdeiassistant.core.secret.mapper",
        "cn.gdeiassistant.core.topic.mapper",
        "cn.gdeiassistant.core.user.mapper"
}, sqlSessionFactoryRef = "appSqlSessionFactory")
public class AppDataSourceConfig {

    @Primary
    @Bean(name = "appDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.app")
    public HikariDataSource appDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "appSqlSessionFactory")
    public SqlSessionFactoryBean appSqlSessionFactory(@Qualifier("appDataSource") DataSource appDataSource) throws IOException {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        bean.setTypeAliasesPackage("cn.gdeiassistant.pojo,cn.gdeiassistant.core");
        bean.setTypeHandlersPackage("cn.gdeiassistant.common.typehandler");
        bean.setDataSource(appDataSource);
        return bean;
    }

    @Primary
    @Bean(name = "appTransactionManager")
    public DataSourceTransactionManager appTransactionManager(@Qualifier("appDataSource") DataSource appDataSource) {
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(appDataSource);
        return tm;
    }
}
