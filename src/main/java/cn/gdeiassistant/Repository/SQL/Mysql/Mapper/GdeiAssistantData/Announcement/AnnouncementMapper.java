package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData.Announcement;

import cn.gdeiassistant.Pojo.Entity.Announcement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

public interface AnnouncementMapper {

    @Results({
            @Result(property = "id", column = "id", id = true),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("select * from announcement order by publish_time desc limit 1")
    Announcement queryLatestAnnouncement();

    @Insert("insert into announcement (title,content,publish_time) values(#{title},#{content},#{publishTime})")
    void insertAnnouncement(Announcement announcement);

}
