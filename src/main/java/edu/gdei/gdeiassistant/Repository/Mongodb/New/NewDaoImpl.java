package edu.gdei.gdeiassistant.Repository.Mongodb.New;

import edu.gdei.gdeiassistant.Pojo.Entity.NewInfo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class NewDaoImpl implements NewDao {

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public void saveNewInfoList(List<NewInfo> newInfoList) {
        for (NewInfo newInfo : newInfoList) {
            mongoTemplate.insert(newInfo, "new");
        }
    }

    @Override
    public NewInfo queryNewInfo(String id) {
        return mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), NewInfo.class);
    }

    @Override
    public List<NewInfo> queryNewInfoList(int type, int start, int size) {
        return mongoTemplate.find(new Query(Criteria.where("type").is(type)).with(new Sort(Sort.Direction.DESC, "publishDate"))
                .skip(start).limit(size), NewInfo.class);
    }
}
