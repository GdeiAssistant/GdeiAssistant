package cn.gdeiassistant.common.config.DataSource;

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
        "cn.gdeiassistant.core.charge.mapper",
        "cn.gdeiassistant.core.close.mapper",
        "cn.gdeiassistant.core.logdata.mapper",
        "cn.gdeiassistant.core.iPAddress.mapper"
}, sqlSessionFactoryRef = "logSqlSessionFactory")
public class LogDataSourceConfig {

    @Bean(name = "logDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.log")
    public DataSource logDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "logSqlSessionFactory")
    public SqlSessionFactoryBean logSqlSessionFactory(@Qualifier("logDataSource") DataSource logDataSource) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        bean.setTypeAliasesPackage("cn.gdeiassistant.pojo,cn.gdeiassistant.core");
        bean.setTypeHandlersPackage("cn.gdeiassistant.common.typehandler");
        bean.setDataSource(logDataSource);
        return bean;
    }

    @Bean(name = "logTransactionManager")
    public DataSourceTransactionManager logTransactionManager(@Qualifier("logDataSource") DataSource logDataSource) {
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(logDataSource);
        return tm;
    }
}
