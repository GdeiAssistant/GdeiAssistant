package cn.gdeiassistant.core.news.repository;

import cn.gdeiassistant.common.pojo.entity.NewsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NewsRepositoryImpl implements NewsRepository {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public void saveNewsItems(List<NewsItem> newsItems){
        if (mongoTemplate != null) {
            for (NewsItem newsItem : newsItems) {
                mongoTemplate.save(newsItem, "new");
            }
        }
    }

    @Override
    public NewsItem queryNewsItem(String id) {
        if (mongoTemplate != null) {
            return mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), NewsItem.class);
        }
        return null;
    }

    @Override
    public List<NewsItem> queryNewsItems(int type, int start, int size) {
        if (mongoTemplate != null) {
            return mongoTemplate.find(new Query(Criteria.where("type").is(type)).with(Sort.by(Sort.Direction.DESC, "publishDate"))
                    .skip(start).limit(size), NewsItem.class);
        }
        return null;
    }
}
