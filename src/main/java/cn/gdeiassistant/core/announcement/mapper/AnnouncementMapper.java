package cn.gdeiassistant.core.announcement.mapper;

import cn.gdeiassistant.core.information.pojo.entity.AnnouncementEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface AnnouncementMapper {

    @Results({
            @Result(property = "id", column = "id", id = true),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    @Select("select * from announcement order by publish_time desc, id desc limit 1")
    AnnouncementEntity queryLatestAnnouncement();

    @Select("select * from announcement where id=#{id} limit 1")
    @Results(id = "Announcement", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    AnnouncementEntity queryAnnouncementById(@Param("id") Integer id);

    @Select("select * from announcement order by publish_time desc, id desc limit #{start},#{size}")
    @ResultMap("Announcement")
    List<AnnouncementEntity> queryAnnouncementPage(@Param("start") Integer start, @Param("size") Integer size);

    @Insert("insert into announcement (title,content,publish_time) values(#{title},#{content},#{publishTime})")
    void insertAnnouncement(AnnouncementEntity announcement);

}
