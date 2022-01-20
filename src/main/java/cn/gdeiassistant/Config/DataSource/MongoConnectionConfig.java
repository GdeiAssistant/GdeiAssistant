package cn.gdeiassistant.Config.DataSource;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;

@Configuration
@PropertySource("classpath:/config/mongodb/mongodb-config.properties")
public class MongoConnectionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

    /**
     * 配置MongoTemplate
     *
     * @return
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        String connectionString = environment.getProperty("mongo.connection.string");
        String database = environment.getProperty("mongo.connection.database");
        if (StringUtils.isNotBlank(connectionString) && StringUtils.isNotBlank(database)) {
            MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDbFactory(MongoClients
                    .create(connectionString), database));
            mongoTemplate.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
            return mongoTemplate;
        }
        //未完整配置Mongodb，禁用Mongodb功能模块
        moduleUtils.DisableModule(ModuleEnum.MONGODB);
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
