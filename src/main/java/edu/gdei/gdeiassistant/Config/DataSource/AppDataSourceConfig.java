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
@MapperScan(basePackages = "edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant", sqlSessionFactoryRef = "appSqlSessionFactory")
@PropertySource("classpath:/config/sql/mysql.properties")
public class AppDataSourceConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 开发环境应用数据源
     *
     * @return
     */
    @Bean(name = "appDataSource")
    @Profile("development")
    @Qualifier("appDevelopmentDataSource")
    public ComboPooledDataSource appDevelopmentDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(environment.getProperty("app.jdbc.driverClass"));
        comboPooledDataSource.setJdbcUrl(environment.getProperty("app.jdbc.jdbcUrl"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.jdbc.minPoolSize"))));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.jdbc.maxPoolSize"))));
        comboPooledDataSource.setMaxIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.jdbc.maxIdleTime"))));
        comboPooledDataSource.setUser(environment.getProperty("app.jdbc.dev.username"));
        comboPooledDataSource.setPassword(environment.getProperty("app.jdbc.dev.password"));
        return comboPooledDataSource;
    }

    /**
     * 生产环境应用数据源
     *
     * @return
     * @throws PropertyVetoException
     */
    @Bean(name = "appDataSource")
    @Profile("production")
    @Qualifier("appProductionDataSource")
    public ComboPooledDataSource appProductionDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(environment.getProperty("app.jdbc.driverClass"));
        comboPooledDataSource.setJdbcUrl(environment.getProperty("app.jdbc.jdbcUrl"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.jdbc.minPoolSize"))));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.jdbc.maxPoolSize"))));
        comboPooledDataSource.setMaxIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("app.jdbc.maxIdleTime"))));
        comboPooledDataSource.setUser(environment.getProperty("app.jdbc.pro.username"));
        comboPooledDataSource.setPassword(environment.getProperty("app.jdbc.pro.password"));
        return comboPooledDataSource;
    }

    /**
     * 开发环境应用SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "appSqlSessionFactory")
    @Profile("development")
    public SqlSessionFactoryBean appDevelopmentSqlSessionFactory(@Qualifier("appDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 生产环境应用SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "appSqlSessionFactory")
    @Profile("production")
    public SqlSessionFactoryBean appProductionSqlSessionFactory(@Qualifier("appDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 开发环境应用事务管理器
     *
     * @return
     */
    @Bean(name = "appTransactionManager")
    @Profile("development")
    public DataSourceTransactionManager appDevelopmentTransactionManager(@Qualifier("appDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    /**
     * 生产环境应用事务管理器
     *
     * @return
     */
    @Bean(name = "appTransactionManager")
    @Profile("production")
    public DataSourceTransactionManager appProductionTransactionManager(@Qualifier("appDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
