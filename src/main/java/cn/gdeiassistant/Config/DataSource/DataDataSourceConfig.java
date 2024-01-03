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
@MapperScan(basePackages = "cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData", sqlSessionFactoryRef = "dataSqlSessionFactory")
@PropertySource("classpath:/config/sql/mysql.properties")
public class DataDataSourceConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

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
        String driverClass = environment.getProperty("jdbc.driverClass");
        String jdbcUrl = environment.getProperty("data.jdbc.dev.jdbcUrl");
        String minPoolSize = environment.getProperty("data.jdbc.minPoolSize");
        String maxPoolSize = environment.getProperty("data.jdbc.maxPoolSize");
        String maxIdleTime = environment.getProperty("data.jdbc.maxIdleTime");
        String user = environment.getProperty("data.jdbc.dev.username");
        String password = environment.getProperty("data.jdbc.dev.password");
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
        String driverClass = environment.getProperty("jdbc.driverClass");
        String jdbcUrl = environment.getProperty("data.jdbc.pro.jdbcUrl");
        String minPoolSize = environment.getProperty("data.jdbc.minPoolSize");
        String maxPoolSize = environment.getProperty("data.jdbc.maxPoolSize");
        String maxIdleTime = environment.getProperty("data.jdbc.maxIdleTime");
        String user = environment.getProperty("data.jdbc.pro.username");
        String password = environment.getProperty("data.jdbc.pro.password");
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
     * 开发环境数据SqlSessionFactory
     *
     * @return
     */
    @Bean(name = "dataSqlSessionFactory")
    @Profile("development")
    public SqlSessionFactoryBean dataDevelopmentSqlSessionFactory(@Qualifier("dataDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/config/sql/mybatis-config.xml"));
        sqlSessionFactoryBean.setTypeAliasesPackage("cn.gdeiassistant.Pojo.Alias");
        sqlSessionFactoryBean.setTypeHandlersPackage("cn.gdeiassistant.TypeHandler");
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
        sqlSessionFactoryBean.setTypeAliasesPackage("cn.gdeiassistant.Pojo.Alias");
        sqlSessionFactoryBean.setTypeHandlersPackage("cn.gdeiassistant.TypeHandler");
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
