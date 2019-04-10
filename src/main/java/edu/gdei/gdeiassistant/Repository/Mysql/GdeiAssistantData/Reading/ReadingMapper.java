package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantData.Reading;

import edu.gdei.gdeiassistant.Pojo.Entity.Reading;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface ReadingMapper {

    @Select("select * from reading order by create_time asc")
    @Results(id = "Reading", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "link", column = "link"),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
    })
    public List<Reading> selectReadingList();

    @Select("select * from reading where id=#{id}")
    @ResultMap("Reading")
    public Reading selectReadingById(String id);

    @Insert("insert into reading (id,title,description,link,create_time) values(#{id},#{title},#{description},#{link},#{createTime})")
    public void insertReading(Reading reading);

    @Update("update reading set title=#{title},description=#{description},link=#{link},create_time=#{createTime} where id=#{id}")
    public void updateReading(Reading reading);
}
