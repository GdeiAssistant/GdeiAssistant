package cn.gdeiassistant.common.config.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {
        "cn.gdeiassistant.core.announcement.mapper",
        "cn.gdeiassistant.core.electricFees.mapper",
        "cn.gdeiassistant.core.yellowPage.mapper"
}, sqlSessionFactoryRef = "dataSqlSessionFactory")
public class DataDataSourceConfig {

    @Bean(name = "dataDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.data")
    public HikariDataSource dataDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "dataSqlSessionFactory")
    public SqlSessionFactoryBean dataSqlSessionFactory(DataSource dataDataSource) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        bean.setTypeAliasesPackage("cn.gdeiassistant.pojo,cn.gdeiassistant.core");
        bean.setTypeHandlersPackage("cn.gdeiassistant.common.typehandler");
        bean.setDataSource(dataDataSource);
        return bean;
    }

    @Bean(name = "dataTransactionManager")
    public DataSourceTransactionManager dataTransactionManager(@Qualifier("dataDataSource") DataSource dataDataSource) {
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataDataSource);
        return tm;
    }
}
