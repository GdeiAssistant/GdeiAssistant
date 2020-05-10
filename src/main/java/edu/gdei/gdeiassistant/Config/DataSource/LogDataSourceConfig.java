package edu.gdei.gdeiassistant.Config.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Objects;

@Configuration
@MapperScan(basePackages = "edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs", sqlSessionFactoryRef = "logSqlSessionFactory")
@PropertySource("classpath:/config/sql/mysql.properties")
public class LogDataSourceConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 开发环境日志数据源
     *
     * @return
     */
    @Bean(name = "logDataSource")
    @Profile("development")
    @Qualifier("logDevelopmentDataSource")
    public ComboPooledDataSource logDevelopmentDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(environment.getProperty("log.jdbc.driverClass"));
        comboPooledDataSource.setJdbcUrl(environment.getProperty("log.jdbc.jdbcUrl"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("log.jdbc.minPoolSize"))));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("log.jdbc.maxPoolSize"))));
        comboPooledDataSource.setMaxIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("log.jdbc.maxIdleTime"))));
        comboPooledDataSource.setUser(environment.getProperty("log.jdbc.dev.username"));
        comboPooledDataSource.setPassword(environment.getProperty("log.jdbc.dev.password"));
        return comboPooledDataSource;
    }

    /**
     * 生产环境日志数据源
     *
     * @return
     * @throws PropertyVetoException
     */
    @Bean(name = "logDataSource")
    @Profile("production")
    @Qualifier("logProductionDataSource")
    public ComboPooledDataSource logProductionDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(environment.getProperty("log.jdbc.driverClass"));
        comboPooledDataSource.setJdbcUrl(environment.getProperty("log.jdbc.jdbcUrl"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("log.jdbc.minPoolSize"))));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("log.jdbc.maxPoolSize"))));
        comboPooledDataSource.setMaxIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("log.jdbc.maxIdleTime"))));
        comboPooledDataSource.setUser(environment.getProperty("log.jdbc.pro.username"));
        comboPooledDataSource.setPassword(environment.getProperty("log.jdbc.pro.password"));
        return comboPooledDataSource;
    }

    /**
     * 开发环境日志SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "logSqlSessionFactory")
    @Profile("development")
    public SqlSessionFactoryBean logDevelopmentSqlSessionFactory(@Qualifier("logDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 生产环境日志SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "logSqlSessionFactory")
    @Profile("production")
    public SqlSessionFactoryBean logProductionSqlSessionFactory(@Qualifier("logDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 开发环境日志事务管理器
     *
     * @return
     */
    @Bean(name = "logTransactionManager")
    @Profile("development")
    public DataSourceTransactionManager logDevelopmentTransactionManager(@Qualifier("logDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    /**
     * 生产环境日志事务管理器
     *
     * @return
     */
    @Bean(name = "logTransactionManager")
    @Profile("production")
    public DataSourceTransactionManager logProductionTransactionManager(@Qualifier("logDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
