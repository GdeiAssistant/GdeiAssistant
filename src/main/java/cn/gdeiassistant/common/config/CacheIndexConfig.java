package cn.gdeiassistant.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;

/** Duplicate cache owners must be resolved before deployment, not silently discarded. */
@Configuration
@org.springframework.context.annotation.DependsOn("architectureCacheUpgrade")
public class CacheIndexConfig {
    @Autowired(required = false)
    public void createCacheIndexes(MongoTemplate mongoTemplate) {
        for (String collection : new String[]{"grade", "schedule"}) {
            mongoTemplate.indexOps(collection).createIndex(new Index().on("username", Sort.Direction.ASC).unique());
        }
    }
}
