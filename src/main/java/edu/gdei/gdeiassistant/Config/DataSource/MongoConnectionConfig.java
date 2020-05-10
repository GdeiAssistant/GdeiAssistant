package edu.gdei.gdeiassistant.Config.DataSource;

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

import java.util.Objects;

@Configuration
@PropertySource("classpath:/config/mongodb/mongodb-config.properties")
public class MongoConnectionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 配置MongoTemplate
     *
     * @return
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDbFactory(MongoClients
                .create(Objects.requireNonNull(environment.getProperty("mongo.connection.string")))
                , Objects.requireNonNull(environment.getProperty("mongo.connection.database"))));
        mongoTemplate.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
        return mongoTemplate;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
