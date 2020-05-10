package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Topic;

import edu.gdei.gdeiassistant.Pojo.Entity.Topic;
import edu.gdei.gdeiassistant.Pojo.Entity.TopicLike;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TopicMapper {

    @Select("select t.id,t.username,t.topic,t.content,t.count,t.publish_time," +
            "sum(distinct CASE WHEN tl.username=#{username} THEN 1 ELSE 0 END) as liked," +
            "count(distinct tl.topic_id) as like_count " +
            "from topic t " +
            "left join topic_like tl on t.id=tl.topic_id " +
            "group by t.id order by t.id limit #{start},#{size}")
    @Results(id = "Topic", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "topic", column = "topic"),
            @Result(property = "content", column = "content"),
            @Result(property = "count", column = "count"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "liked", column = "liked")

    })
    public List<Topic> selectTopicPage(@Param("start") int start, @Param("size") int size, @Param("username") String username);

    @Select("select t.id,t.username,t.topic,t.content,t.count,t.publish_time," +
            "sum(distinct CASE WHEN tl.username=#{username} THEN 1 ELSE 0 END) as liked," +
            "count(distinct tl.topic_id) as like_count " +
            "from topic t " +
            "left join topic_like tl on t.id=tl.topic_id " +
            "where t.topic like concat(concat('%',#{keyword}),'%') group by t.id order by t.id limit #{start},#{size}")
    @ResultMap("Topic")
    public List<Topic> selectTopicPageByKeyword(@Param("start") int start, @Param("size") int size, @Param("username") String username, @Param("keyword") String keyword);

    @Select("select t.id,t.username,t.topic,t.content,t.count,t.publish_time," +
            "sum(distinct CASE WHEN tl.username=#{username} THEN 1 ELSE 0 END) as liked," +
            "count(distinct tl.topic_id) as like_count " +
            "from topic t " +
            "left join topic_like tl on t.id=tl.topic_id " +
            "where t.id=#{id} group by t.id")
    @ResultMap("Topic")
    public Topic selectTopicById(@Param("id") int id,@Param("username") String username);

    @Insert("insert into topic (username,topic,content,count,publish_time) values(#{username},#{topic},#{content},#{count},now())")
    @Options(useGeneratedKeys = true)
    public void insertTopic(Topic topic);

    @Select("select id from topic_like where topic_id=#{id} and username=#{username}")
    @Results(id = "TopicLike", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "topicId", column = "topic_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "createTime", column = "create_time")
    })
    public TopicLike selectTopicLike(@Param("id") int id, @Param("username") String username);

    @Insert("insert into topic_like (topic_id,username,create_time) values(#{topicId},#{username},now())")
    public void insertTopicLike(@Param("topicId") int id, @Param("username") String username);
}
