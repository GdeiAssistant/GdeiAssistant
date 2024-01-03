package cn.gdeiassistant.Config.DataSource;

import cn.gdeiassistant.Enum.Module.CoreModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@MapperScan(basePackages = "cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs", sqlSessionFactoryRef = "logSqlSessionFactory")
@PropertySource("classpath:/config/sql/mysql.properties")
public class LogDataSourceConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

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
        String driverClass = environment.getProperty("jdbc.driverClass");
        String jdbcUrl = environment.getProperty("log.jdbc.dev.jdbcUrl");
        String minPoolSize = environment.getProperty("log.jdbc.minPoolSize");
        String maxPoolSize = environment.getProperty("log.jdbc.maxPoolSize");
        String maxIdleTime = environment.getProperty("log.jdbc.maxIdleTime");
        String user = environment.getProperty("log.jdbc.dev.username");
        String password = environment.getProperty("log.jdbc.dev.password");
        if (StringUtils.isNotBlank(driverClass) && StringUtils.isNotBlank(jdbcUrl) && StringUtils.isNotBlank(minPoolSize)
                && StringUtils.isNotBlank(maxPoolSize) && StringUtils.isNotBlank(maxIdleTime)
                && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            if (StringUtils.isNumeric(minPoolSize) && StringUtils.isNumeric(maxPoolSize) && StringUtils.isNumeric(maxIdleTime)) {
                comboPooledDataSource.setDriverClass(driverClass);
                comboPooledDataSource.setJdbcUrl(jdbcUrl);
                comboPooledDataSource.setMinPoolSize(Integer.parseInt(minPoolSize));
                comboPooledDataSource.setMaxPoolSize(Integer.parseInt(maxPoolSize));
                comboPooledDataSource.setMaxIdleTime(Integer.parseInt(maxIdleTime));
                comboPooledDataSource.setUser(user);
                comboPooledDataSource.setPassword(password);
                return comboPooledDataSource;
            }
        }
        moduleUtils.DisableCoreModule(CoreModuleEnum.MYSQL);
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
        String driverClass = environment.getProperty("jdbc.driverClass");
        String jdbcUrl = environment.getProperty("log.jdbc.pro.jdbcUrl");
        String minPoolSize = environment.getProperty("log.jdbc.minPoolSize");
        String maxPoolSize = environment.getProperty("log.jdbc.maxPoolSize");
        String maxIdleTime = environment.getProperty("log.jdbc.maxIdleTime");
        String user = environment.getProperty("log.jdbc.pro.username");
        String password = environment.getProperty("log.jdbc.pro.password");
        if (StringUtils.isNotBlank(driverClass) && StringUtils.isNotBlank(jdbcUrl) && StringUtils.isNotBlank(minPoolSize)
                && StringUtils.isNotBlank(maxPoolSize) && StringUtils.isNotBlank(maxIdleTime)
                && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            if (StringUtils.isNumeric(minPoolSize) && StringUtils.isNumeric(maxPoolSize) && StringUtils.isNumeric(maxIdleTime)) {
                comboPooledDataSource.setDriverClass(driverClass);
                comboPooledDataSource.setJdbcUrl(jdbcUrl);
                comboPooledDataSource.setMinPoolSize(Integer.parseInt(minPoolSize));
                comboPooledDataSource.setMaxPoolSize(Integer.parseInt(maxPoolSize));
                comboPooledDataSource.setMaxIdleTime(Integer.parseInt(maxIdleTime));
                comboPooledDataSource.setUser(user);
                comboPooledDataSource.setPassword(password);
                return comboPooledDataSource;
            }
        }
        moduleUtils.DisableCoreModule(CoreModuleEnum.MYSQL);
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
        sqlSessionFactoryBean.setTypeAliasesPackage("cn.gdeiassistant.Pojo.Alias");
        sqlSessionFactoryBean.setTypeHandlersPackage("cn.gdeiassistant.TypeHandler");
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
        sqlSessionFactoryBean.setTypeAliasesPackage("cn.gdeiassistant.Pojo.Alias");
        sqlSessionFactoryBean.setTypeHandlersPackage("cn.gdeiassistant.TypeHandler");
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
