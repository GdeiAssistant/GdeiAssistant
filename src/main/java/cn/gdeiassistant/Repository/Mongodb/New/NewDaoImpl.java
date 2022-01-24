package cn.gdeiassistant.Repository.Mongodb.New;

import cn.gdeiassistant.Exception.DatasourceException.MongodbNotConfiguredException;
import cn.gdeiassistant.Pojo.Entity.NewInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NewDaoImpl implements NewDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public void saveNewInfoList(List<NewInfo> newInfoList) throws MongodbNotConfiguredException {
        if (mongoTemplate != null) {
            for (NewInfo newInfo : newInfoList) {
                mongoTemplate.insert(newInfo, "new");
            }
        }
        throw new MongodbNotConfiguredException("MongoDB数据源未配置");
    }

    @Override
    public NewInfo queryNewInfo(String id) {
        if (mongoTemplate != null) {
            return mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), NewInfo.class);
        }
        return null;
    }

    @Override
    public List<NewInfo> queryNewInfoList(int type, int start, int size) {
        if (mongoTemplate != null) {
            return mongoTemplate.find(new Query(Criteria.where("type").is(type)).with(new Sort(Sort.Direction.DESC, "publishDate"))
                    .skip(start).limit(size), NewInfo.class);
        }
        return null;
    }
}
