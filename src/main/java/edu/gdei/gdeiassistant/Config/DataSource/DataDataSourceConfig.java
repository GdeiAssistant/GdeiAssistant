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
@MapperScan(basePackages = "edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData", sqlSessionFactoryRef = "dataSqlSessionFactory")
@PropertySource("classpath:/config/sql/mysql.properties")
public class DataDataSourceConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 开发环境数据数据源
     *
     * @return
     */
    @Bean(name = "dataDataSource")
    @Profile("development")
    @Qualifier("dataDevelopmentDataSource")
    public ComboPooledDataSource dataDevelopmentDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(environment.getProperty("data.jdbc.driverClass"));
        comboPooledDataSource.setJdbcUrl(environment.getProperty("data.jdbc.jdbcUrl"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("data.jdbc.minPoolSize"))));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("data.jdbc.maxPoolSize"))));
        comboPooledDataSource.setMaxIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("data.jdbc.maxIdleTime"))));
        comboPooledDataSource.setUser(environment.getProperty("data.jdbc.dev.username"));
        comboPooledDataSource.setPassword(environment.getProperty("data.jdbc.dev.password"));
        return comboPooledDataSource;
    }

    /**
     * 生产环境数据数据源
     *
     * @return
     * @throws PropertyVetoException
     */
    @Bean(name = "dataDataSource")
    @Profile("production")
    @Qualifier("dataProductionDataSource")
    public ComboPooledDataSource dataProductionDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(environment.getProperty("data.jdbc.driverClass"));
        comboPooledDataSource.setJdbcUrl(environment.getProperty("data.jdbc.jdbcUrl"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("data.jdbc.minPoolSize"))));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(environment.getProperty("data.jdbc.maxPoolSize"))));
        comboPooledDataSource.setMaxIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("data.jdbc.maxIdleTime"))));
        comboPooledDataSource.setUser(environment.getProperty("data.jdbc.pro.username"));
        comboPooledDataSource.setPassword(environment.getProperty("data.jdbc.pro.password"));
        return comboPooledDataSource;
    }

    /**
     * 开发环境数据SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "dataSqlSessionFactory")
    @Profile("development")
    public SqlSessionFactoryBean dataDevelopmentSqlSessionFactory(@Qualifier("dataDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 生产环境数据SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "dataSqlSessionFactory")
    @Profile("production")
    public SqlSessionFactoryBean dataProductionSqlSessionFactory(@Qualifier("dataDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }

    /**
     * 开发环境数据事务管理器
     *
     * @return
     */
    @Bean(name = "dataTransactionManager")
    @Profile("development")
    public DataSourceTransactionManager dataDevelopmentTransactionManager(@Qualifier("dataDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    /**
     * 生产环境数据事务管理器
     *
     * @return
     */
    @Bean(name = "dataTransactionManager")
    @Profile("production")
    public DataSourceTransactionManager dataProductionTransactionManager(@Qualifier("dataDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
