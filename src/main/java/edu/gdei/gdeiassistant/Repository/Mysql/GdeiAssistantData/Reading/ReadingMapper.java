package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantData.Reading;

import edu.gdei.gdeiassistant.Pojo.Entity.Reading;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ReadingMapper {

    @Select("select * from reading")
    @ResultType(Reading.class)
    public List<Reading> selectReadingList();

    @Select("select * from reading where id=#{id}")
    @ResultType(Reading.class)
    public Reading selectReadingById(String id);

    @Insert("insert into reading (id,title,description,link) values(#{id},#{title},#{description},#{link})")
    public void insertReading(Reading reading);
}
