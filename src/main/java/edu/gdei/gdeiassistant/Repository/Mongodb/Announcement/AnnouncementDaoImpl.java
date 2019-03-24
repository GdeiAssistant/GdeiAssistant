package edu.gdei.gdeiassistant.Repository.Mongodb.Announcement;

import edu.gdei.gdeiassistant.Pojo.Entity.Announcement;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class AnnouncementDaoImpl implements AnnouncementDao {

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public Announcement QueryLatestAnnouncement() {
        return mongoTemplate.findOne(new Query().with(new Sort(Sort.Direction.DESC, "publishTime")).limit(1)
                , Announcement.class, "announcement");
    }

    @Override
    public void InsertAnnouncement(Announcement announcement) {
        mongoTemplate.insert(announcement);
    }
}
