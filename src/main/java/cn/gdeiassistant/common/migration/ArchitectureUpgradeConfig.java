package cn.gdeiassistant.common.migration;

import javax.sql.DataSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import java.sql.SQLException;

@Configuration
public class ArchitectureUpgradeConfig {
    @Bean("architectureCacheUpgrade")
    @org.springframework.context.annotation.DependsOn("architectureDatabaseUpgrade")
    public Object cache(org.springframework.beans.factory.ObjectProvider<org.springframework.data.mongodb.core.MongoTemplate> provider,
            @Value("${architecture.migration.enabled:false}") boolean enabled) {
        var mongo = provider.getIfAvailable();
        if (enabled && mongo != null) ArchitectureCacheUpgrade.migrate(mongo);
        return new Object();
    }

    @Bean("architectureDatabaseUpgrade")
    public Object apply(@Qualifier("appDataSource") DataSource app,
            @Qualifier("dataDataSource") DataSource data, @Qualifier("logDataSource") DataSource log,
            @Value("${architecture.migration.enabled:false}") boolean enabled) throws SQLException {
        if (enabled) {
            ArchitectureDatabaseUpgrade.migrate(app,data,log);
            org.slf4j.LoggerFactory.getLogger(getClass()).info("Architecture database upgrade completed; row counts preserved");
        }
        return new Object();
    }
}
