package edu.gdei.gdeiassistant.Config;

import com.mongodb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.Objects;

@Configuration
@PropertySource("classpath:/config/mongodb/mongodb-config.properties")
public class MongoConnectionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 配置MongoDBFactory
     *
     * @return
     */
    @Bean
    public MongoDbFactory mongoDbFactory() {
        MongoClient mongoClient = new MongoClient(new ServerAddress(environment.getProperty("mongo.host")
                , Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.port"))))
                , MongoCredential.createCredential(Objects.requireNonNull(environment.getProperty("mongo.username"))
                , Objects.requireNonNull(environment.getProperty("mongo.dbname"))
                , Objects.requireNonNull(environment.getProperty("mongo.password")).toCharArray())
                , new MongoClientOptions.Builder()
                .connectionsPerHost(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.connectionsPerHost"))))
                .minConnectionsPerHost(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.minConnectionsPerHost"))))
                .threadsAllowedToBlockForConnectionMultiplier(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.threadsAllowedToBlockForConnectionMultiplier"))))
                .connectTimeout(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.connectTimeout"))))
                .maxWaitTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.maxWaitTime"))))
                .socketTimeout(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.socketTimeout"))))
                .maxConnectionIdleTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.maxConnectionIdleTime"))))
                .maxConnectionLifeTime(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.maxConnectionLifeTime"))))
                .heartbeatSocketTimeout(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.heartbeatSocketTimeout"))))
                .heartbeatConnectTimeout(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.heartbeatConnectTimeout"))))
                .minHeartbeatFrequency(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.minHeartbeatFrequency"))))
                .heartbeatFrequency(Integer.parseInt(Objects.requireNonNull(environment.getProperty("mongo.heartbeatFrequency")))).build());
        return new SimpleMongoDbFactory(mongoClient, Objects.requireNonNull(environment.getProperty("mongo.dbname")));
    }

    /**
     * 配置MongoTemplate
     *
     * @return
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        mongoTemplate.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
        return mongoTemplate;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
