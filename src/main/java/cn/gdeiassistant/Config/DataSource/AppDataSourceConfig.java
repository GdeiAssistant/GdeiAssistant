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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.IOException;

@Configuration
@MapperScan(basePackages = "cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant", sqlSessionFactoryRef = "appSqlSessionFactory")
@PropertySource("classpath:/config/sql/mysql.properties")
public class AppDataSourceConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

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
        String driverClass = environment.getProperty("app.jdbc.driverClass");
        String jdbcUrl = environment.getProperty("app.jdbc.dev.jdbcUrl");
        String minPoolSize = environment.getProperty("app.jdbc.minPoolSize");
        String maxPoolSize = environment.getProperty("app.jdbc.maxPoolSize");
        String maxIdleTime = environment.getProperty("app.jdbc.maxIdleTime");
        String user = environment.getProperty("app.jdbc.dev.username");
        String password = environment.getProperty("app.jdbc.dev.password");
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
        String driverClass = environment.getProperty("app.jdbc.driverClass");
        String jdbcUrl = environment.getProperty("app.jdbc.pro.jdbcUrl");
        String minPoolSize = environment.getProperty("app.jdbc.minPoolSize");
        String maxPoolSize = environment.getProperty("app.jdbc.maxPoolSize");
        String maxIdleTime = environment.getProperty("app.jdbc.maxIdleTime");
        String user = environment.getProperty("app.jdbc.pro.username");
        String password = environment.getProperty("app.jdbc.pro.password");
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
     * 开发环境应用SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "appSqlSessionFactory")
    @Profile("development")
    public SqlSessionFactoryBean appDevelopmentSqlSessionFactory(@Qualifier("appDataSource") DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setTypeAliasesPackage("cn.gdeiassistant.Pojo.Alias");
        sqlSessionFactoryBean.setTypeHandlersPackage("cn.gdeiassistant.TypeHandler");
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
    public SqlSessionFactoryBean appProductionSqlSessionFactory(@Qualifier("appDataSource") DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setTypeAliasesPackage("cn.gdeiassistant.Pojo.Alias");
        sqlSessionFactoryBean.setTypeHandlersPackage("cn.gdeiassistant.TypeHandler");
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
